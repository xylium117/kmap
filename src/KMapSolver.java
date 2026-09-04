package src;

import java.util.*;
import java.util.stream.Collectors;

public class KMapSolver {
    private final int vars;
    private final Set<Integer> minterms;
    private final Set<Integer> dontCares;
    private final List<String> keyPrimes;
    private List<String> posPrimes;

    public KMapSolver(int vars, Set<Integer> minterms, Set<Integer> dontCares) {
        if (vars < 1 || vars > 6) {
            throw new IllegalArgumentException("Supported number of variables is between 1 and 6");
        }
        this.vars = vars;
        this.minterms = minterms != null ? new HashSet<>(minterms) : new HashSet<>();
        this.dontCares = dontCares != null ? new HashSet<>(dontCares) : new HashSet<>();
        verify();

        String allDashes = dashString(vars);

        if (this.minterms.isEmpty()) {
            this.keyPrimes = Collections.emptyList();
        } else if (this.minterms.size() + this.dontCares.size() == (1 << vars)) {
            List<String> primes = getQMPrimes(combine());
            if (primes.contains(allDashes)) {
                this.keyPrimes = Collections.singletonList(allDashes);
            } else {
                this.keyPrimes = getKeyPrimes(primes, this.minterms);
            }
        } else {
            List<String> primeImpls = getQMPrimes(combine());
            this.keyPrimes = getKeyPrimes(primeImpls, this.minterms);
        }

        computePOSPrimes();
    }

    private static String dashString(int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, '-');
        return new String(chars);
    }

    private void computePOSPrimes() {
        int totalStates = 1 << vars;
        String allDashes = dashString(vars);

        if (minterms.isEmpty()) {
            this.posPrimes = Collections.emptyList();
            return;
        }
        if (minterms.size() + dontCares.size() == totalStates) {
            if (getSOP().equals("1")) {
                this.posPrimes = Collections.singletonList(allDashes);
                return;
            }
        }

        Set<Integer> maxterms = new HashSet<>();
        for (int i = 0; i < totalStates; i++) {
            if (!minterms.contains(i) && !dontCares.contains(i)) {
                maxterms.add(i);
            }
        }

        if (maxterms.isEmpty()) {
            this.posPrimes = Collections.singletonList(allDashes);
            return;
        }

        Set<Integer> compCombined = new HashSet<>(maxterms);
        compCombined.addAll(dontCares);
        List<String> compPrimes = getQMPrimes(compCombined);
        this.posPrimes = getKeyPrimes(compPrimes, maxterms);
    }

    private Set<Integer> combine() {
        Set<Integer> combined = new HashSet<>(minterms);
        combined.addAll(dontCares);
        return combined;
    }

    private void verify() {
        int maxTerm = (1 << vars) - 1;
        for (int term : minterms) {
            if (term < 0 || term > maxTerm) {
                throw new IllegalArgumentException("Invalid minterm: " + term + " (allowed 0 to " + maxTerm + ")");
            }
        }
        for (int term : dontCares) {
            if (term < 0 || term > maxTerm) {
                throw new IllegalArgumentException("Invalid Don't-Care: " + term + " (allowed 0 to " + maxTerm + ")");
            }
            if (minterms.contains(term)) {
                throw new IllegalArgumentException("Term cannot be both minterm and don't-care: " + term);
            }
        }
    }

    private List<String> getQMPrimes(Set<Integer> terms) {
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> binaryTerms = terms.stream()
                .map(this::toBin)
                .collect(Collectors.toList());

        List<List<String>> groups = groupByOnes(binaryTerms);
        List<String> primes = new ArrayList<>();

        while (true) {
            List<List<String>> newGroups = new ArrayList<>();
            for (int i = 0; i <= vars; i++) {
                newGroups.add(new ArrayList<>());
            }
            Set<String> marked = new HashSet<>();

            for (int i = 0; i < groups.size() - 1; i++) {
                List<String> group1 = groups.get(i);
                List<String> group2 = groups.get(i + 1);

                for (String term1 : group1) {
                    for (String term2 : group2) {
                        String combined = combineTerms(term1, term2);
                        if (combined != null) {
                            marked.add(term1);
                            marked.add(term2);
                            int ones = countOnes(combined);
                            if (!newGroups.get(ones).contains(combined)) {
                                newGroups.get(ones).add(combined);
                            }
                        }
                    }
                }
            }

            for (List<String> group : groups) {
                for (String term : group) {
                    if (!marked.contains(term) && !primes.contains(term)) {
                        primes.add(term);
                    }
                }
            }

            boolean hasNew = false;
            for (List<String> g : newGroups) {
                if (!g.isEmpty()) {
                    hasNew = true;
                    break;
                }
            }
            if (!hasNew) break;

            groups = newGroups;
        }

        return primes;
    }

    private List<String> getKeyPrimes(List<String> primes, Set<Integer> terms) {
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }
        if (primes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Set<Integer>> primeMap = new LinkedHashMap<>();
        for (String prime : primes) {
            Set<Integer> coveredMinterms = new HashSet<>(cover(prime));
            coveredMinterms.retainAll(terms);
            if (!coveredMinterms.isEmpty()) {
                primeMap.put(prime, coveredMinterms);
            }
        }

        List<String> candidatePrimes = new ArrayList<>(primeMap.keySet());
        List<String> essentialPrimes = new ArrayList<>();
        Set<Integer> coveredSoFar = new HashSet<>();

        boolean changed;
        do {
            changed = false;
            Map<Integer, List<String>> mintermToPrimes = new HashMap<>();

            for (String prime : candidatePrimes) {
                if (essentialPrimes.contains(prime)) continue;
                for (int m : primeMap.get(prime)) {
                    if (!coveredSoFar.contains(m)) {
                        mintermToPrimes.computeIfAbsent(m, k -> new ArrayList<>()).add(prime);
                    }
                }
            }

            for (Map.Entry<Integer, List<String>> entry : mintermToPrimes.entrySet()) {
                if (entry.getValue().size() == 1) {
                    String essential = entry.getValue().get(0);
                    if (!essentialPrimes.contains(essential)) {
                        essentialPrimes.add(essential);
                        coveredSoFar.addAll(primeMap.get(essential));
                        changed = true;
                    }
                }
            }
        } while (changed);

        if (coveredSoFar.containsAll(terms)) {
            return essentialPrimes;
        }

        Set<Integer> remainingTerms = new HashSet<>(terms);
        remainingTerms.removeAll(coveredSoFar);

        List<String> remainingPrimes = new ArrayList<>(candidatePrimes);
        remainingPrimes.removeAll(essentialPrimes);

        remainingPrimes.sort((p1, p2) -> {
            int c1 = countCovered(primeMap.get(p1), remainingTerms);
            int c2 = countCovered(primeMap.get(p2), remainingTerms);
            if (c1 != c2) return Integer.compare(c2, c1);
            return Integer.compare(countLiterals(p1), countLiterals(p2));
        });

        List<String> bestAdditional = findMinimalCover(remainingPrimes, primeMap, remainingTerms);
        List<String> finalPrimes = new ArrayList<>(essentialPrimes);
        finalPrimes.addAll(bestAdditional);
        return finalPrimes;
    }

    private int countCovered(Set<Integer> primeCoverage, Set<Integer> needed) {
        if (primeCoverage == null) return 0;
        int count = 0;
        for (int m : primeCoverage) {
            if (needed.contains(m)) count++;
        }
        return count;
    }

    private int countLiterals(String prime) {
        int l = 0;
        for (char c : prime.toCharArray()) {
            if (c != '-') l++;
        }
        return l;
    }

    private List<String> findMinimalCover(List<String> candidatePrimes, Map<String, Set<Integer>> primeMap, Set<Integer> uncoveredTerms) {
        if (uncoveredTerms.isEmpty()) {
            return Collections.emptyList();
        }

        Queue<List<String>> queue = new PriorityQueue<>((a, b) -> {
            if (a.size() != b.size()) return Integer.compare(a.size(), b.size());
            int litA = a.stream().mapToInt(this::countLiterals).sum();
            int litB = b.stream().mapToInt(this::countLiterals).sum();
            return Integer.compare(litA, litB);
        });

        Set<Set<String>> visited = new HashSet<>();

        for (String prime : candidatePrimes) {
            List<String> initial = Collections.singletonList(prime);
            queue.add(initial);
            visited.add(new HashSet<>(initial));
        }

        while (!queue.isEmpty()) {
            List<String> current = queue.poll();

            Set<Integer> currentCover = new HashSet<>();
            for (String p : current) {
                Set<Integer> s = primeMap.get(p);
                if (s != null) currentCover.addAll(s);
            }

            if (currentCover.containsAll(uncoveredTerms)) {
                return current;
            }

            for (String prime : candidatePrimes) {
                if (!current.contains(prime)) {
                    Set<String> nextSet = new HashSet<>(current);
                    nextSet.add(prime);
                    if (visited.add(nextSet)) {
                        List<String> nextList = new ArrayList<>(current);
                        nextList.add(prime);
                        queue.add(nextList);
                    }
                }
            }
        }

        return candidatePrimes;
    }

    public Set<Integer> cover(String term) {
        Set<Integer> covered = new HashSet<>();
        List<String> expansions = expandTerm(term);
        for (String expanded : expansions) {
            covered.add(toDec(expanded));
        }
        return covered;
    }

    private List<String> expandTerm(String term) {
        List<String> expansions = new ArrayList<>();
        expand(term, 0, expansions);
        return expansions;
    }

    private void expand(String term, int index, List<String> expansions) {
        if (index == term.length()) {
            expansions.add(term);
            return;
        }

        if (term.charAt(index) == '-') {
            StringBuilder sb0 = new StringBuilder(term);
            sb0.setCharAt(index, '0');
            expand(sb0.toString(), index + 1, expansions);

            StringBuilder sb1 = new StringBuilder(term);
            sb1.setCharAt(index, '1');
            expand(sb1.toString(), index + 1, expansions);
        } else {
            expand(term, index + 1, expansions);
        }
    }

    private List<List<String>> groupByOnes(List<String> terms) {
        List<List<String>> groups = new ArrayList<>();
        for (int i = 0; i <= vars; i++) {
            groups.add(new ArrayList<>());
        }
        for (String term : terms) {
            int count = countOnes(term);
            groups.get(count).add(term);
        }
        return groups;
    }

    private int countOnes(String term) {
        int count = 0;
        for (char c : term.toCharArray()) {
            if (c == '1') count++;
        }
        return count;
    }

    private String combineTerms(String term1, String term2) {
        int diff = -1;
        for (int i = 0; i < term1.length(); i++) {
            if (term1.charAt(i) != term2.charAt(i)) {
                if (diff != -1) return null;
                diff = i;
            }
        }
        if (diff == -1) return null;
        return term1.substring(0, diff) + "-" + term1.substring(diff + 1);
    }

    private String toBin(int n) {
        StringBuilder bin = new StringBuilder(Integer.toBinaryString(n));
        while (bin.length() < vars) {
            bin.insert(0, "0");
        }
        return bin.toString();
    }

    private int toDec(String bin) {
        return Integer.parseInt(bin, 2);
    }

    public String toAlgebraic(String term) {
        StringBuilder sb = new StringBuilder();
        char var = 'A';

        for (int i = 0; i < term.length(); i++) {
            char c = term.charAt(i);
            if (c == '0') {
                sb.append(var).append("'");
            } else if (c == '1') {
                sb.append(var);
            }
            var++;
        }

        return sb.length() == 0 ? "1" : sb.toString();
    }

    public String toPOSAlgebraic(String posTerm) {
        StringBuilder sb = new StringBuilder("(");
        char var = 'A';
        boolean first = true;

        for (int i = 0; i < posTerm.length(); i++) {
            char c = posTerm.charAt(i);
            if (c == '0') {
                if (!first) sb.append(" + ");
                sb.append(var);
                first = false;
            } else if (c == '1') {
                if (!first) sb.append(" + ");
                sb.append(var).append("'");
                first = false;
            }
            var++;
        }

        if (first) return "1";
        sb.append(")");
        return sb.toString();
    }

    public String getSOP() {
        if (minterms.isEmpty()) {
            return "0";
        }
        if (keyPrimes.isEmpty()) {
            return "0";
        }
        String allDashes = dashString(vars);
        if (keyPrimes.contains(allDashes)) {
            return "1";
        }

        List<String> terms = keyPrimes.stream()
                .map(this::toAlgebraic)
                .collect(Collectors.toList());

        if (terms.contains("1")) {
            return "1";
        }

        return String.join(" + ", terms);
    }

    public String getPOS() {
        int totalStates = 1 << vars;
        if (minterms.isEmpty()) {
            return "0";
        }
        if (minterms.size() + dontCares.size() == totalStates) {
            if (getSOP().equals("1")) {
                return "1";
            }
        }

        if (posPrimes == null || posPrimes.isEmpty()) {
            return "0";
        }
        String allDashes = dashString(vars);
        if (posPrimes.contains(allDashes)) {
            return "1";
        }

        List<String> terms = posPrimes.stream()
                .map(this::toPOSAlgebraic)
                .filter(t -> !t.equals("1"))
                .collect(Collectors.toList());

        return terms.isEmpty() ? "1" : String.join(" ", terms);
    }

    public int getVariables() {
        return vars;
    }

    public Set<Integer> getMinterms() {
        return Collections.unmodifiableSet(minterms);
    }

    public Set<Integer> getDontCares() {
        return Collections.unmodifiableSet(dontCares);
    }

    public List<String> getImplicants() {
        return Collections.unmodifiableList(keyPrimes);
    }

    public List<String> getPOSImplicants() {
        return posPrimes != null ? Collections.unmodifiableList(posPrimes) : Collections.emptyList();
    }
}