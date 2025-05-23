package src;

import java.util.*;
import java.util.stream.Collectors;

public class KMapSolver {
    private final int vars;
    private final Set<Integer> minterms;
    private final List<String> keyPrimes;
    private final Set<Integer> dontCares;

    public KMapSolver ( int vars, Set<Integer> minterms, Set<Integer> dontCares ) {
        this.vars = vars;
        this.minterms = new HashSet<>( minterms );
        this.dontCares = new HashSet<>( dontCares );
        verify( );
        List<String> primeImpls = getQMPrimes( combine( ) );
        this.keyPrimes = getKeyPrimes( primeImpls, minterms );
    }


    // Combine minterms and Don't-Cares for prime implicant generation
    private Set<Integer> combine ( ) {
        Set<Integer> combined = new HashSet<>( minterms );
        combined.addAll( dontCares );
        return combined;
    }

    private void verify ( ) {
        int maxTerm = (1 << vars) - 1;
        for ( int term : minterms ) {
            if ( term < 0 || term > maxTerm ) {
                throw new IllegalArgumentException( "Invalid minterm: " + term );
            }
        }
        for ( int term : dontCares ) {
            if ( term < 0 || term > maxTerm ) {
                throw new IllegalArgumentException( "Invalid Don't-Care: " + term );
            }
            if ( minterms.contains( term ) ) {
                throw new IllegalArgumentException( "Term cannot be both minterm and don't-care: " + term );
            }
        }
    }

    /**
     * Finds the prime implicants of the given set of minterms using the Quine-McCluskey algorithm.
     * <p>
     * The Quine-McCluskey (QMC) Algorithm constitutes a deterministic, tabular method for Boolean function minimization that operates through systematic implicant generation and covering analysis. The algorithm proceeds by first partitioning n-variable minterms into equivalence classes Gₖ based on Hamming weight (k = 0,...,n), then iteratively performs pairwise comparisons between adjacent groups to generate (k+1)-cube implicants through application of the consensus theorem (xy + x'z = yz when yz = 0), where single-bit differences induce don't-care states ('-') in the resulting implicant pattern. This combinatorial process exhibits O(3ⁿ/√n) time complexity in the worst case due to the exhaustive comparison tree depth and generates a complete set of prime implicants P that must subsequently undergo covering analysis via Petrick's method or branch-and-bound techniques to extract a minimal cover C ⊆ P satisfying the closure condition ∪_{p∈C} on(p) = M, where on(p) denotes the minterm set covered by prime p and M represents the original on-set. The algorithm's exponential space complexity stems from the necessity to maintain intermediate implicant sets during the generation phase, though its deterministic nature guarantees identification of the exact minimal solution unlike heuristic approaches.
     *
     * @param terms Set of minterms
     * @return List of all prime implicants in binary form (with '-' for Don't-Care positions)
     */
    private List<String> getQMPrimes ( Set<Integer> terms ) {
        // Convert all terms to their binary representations
        List<String> binaryTerms = terms.stream( )
                .map( this::toBin )  // Convert to n-bit binary strings
                .collect( Collectors.toList( ) );

        // ===== PHASE 1: Initial Grouping =====
        // Group terms by the number of 1's in their binary representation (Hamming weight)
        // Groups[0] = all terms with 0 ones, Groups[1] = terms with 1 one, etc.
        List<List<String>> groups = groupByOnes( binaryTerms );
        List<String> primes = new ArrayList<>( );

        // ===== PHASE 2: Iterative Combination =====
        // Continue until no more combinations can be made
        while ( true ) {
            List<List<String>> newGroups = new ArrayList<>( );
            Set<String> marked = new HashSet<>( );  // Tracks combined terms

            // Compare adjacent groups (differing by 1 in Hamming weight)
            for ( int i = 0; i < groups.size( ) - 1; i++ ) {
                List<String> group1 = groups.get( i );    // Group with k ones
                List<String> group2 = groups.get( i + 1 );  // Group with k+1 ones
                List<String> newGroup = new ArrayList<>( );

                // Compare all terms between adjacent groups
                for ( String term1 : group1 ) {
                    for ( String term2 : group2 ) {
                        // Attempt to combine terms that differ by exactly 1 bit
                        String combined = combineTerms( term1, term2 );
                        if ( combined != null ) {
                            marked.add( term1 );  // Mark as combined
                            marked.add( term2 );
                            if ( !newGroup.contains( combined ) ) {
                                newGroup.add( combined );  // Add new implicant
                            }
                        }
                    }
                }

                // Add non-empty combinations to next iteration
                if ( !newGroup.isEmpty( ) ) {
                    newGroups.add( newGroup );
                }
            }

            // ===== PHASE 3: Prime Identification =====
            // Any uncombined term in current iteration is a prime implicant
            for ( List<String> group : groups ) {
                for ( String term : group ) {
                    if ( !marked.contains( term ) && !primes.contains( term ) ) {
                        primes.add( term );  // Found a prime implicant
                    }
                }
            }

            // Break condition: no new combinations made
            if ( newGroups.isEmpty( ) ) break;

            // Prepare for next iteration with the new combined terms
            groups = newGroups;
        }

        return primes;
    }

    /**
     * Applies Petrick's method to find a minimal cover of the minterms
     * not covered by the key prime implicants.
     * <p>
     * Petrick’s method is a rigorous combinational approach employed in Boolean function minimization to identify all minimal prime implicant covers beyond the essential set derived from the Quine–McCluskey algorithm. When essential(or key) prime implicants fail to fully cover the function’s minterms, Petrick’s method constructs a canonical logical formulation—expressed as a product of sums—that encapsulates every feasible prime implicant combination capable of covering the uncovered minterms. By algebraically expanding and minimizing this expression, it systematically enumerates the minimal solutions that guarantee complete coverage with optimal term count. While Petrick’s method provides an exact and exhaustive solution, its computational complexity escalates exponentially with problem size, rendering it challenging for large-scale instances. Nevertheless, it remains a foundational algorithmic tool in digital logic synthesis, prized for its mathematical rigor and precision in achieving optimal logic simplification.
     *
     * @param primes The list of prime implicants obtained from QMC Algorithm
     * @param terms  The full set of minterms that must be covered
     * @return A minimal list of QMC Prime Implicants
     */
    private List<String> getKeyPrimes ( List<String> primes, Set<Integer> terms ) {
        // === PHASE 1: Build Coverage Map (ignoring Don't-Care coverage) ===
        Map<String, Set<Integer>> primeMap = new HashMap<>( );
        for ( String prime : primes ) {
            // Get coverage and retain only required minterms
            Set<Integer> baseMap = new HashSet<>( cover( prime ) );
            baseMap.retainAll( terms );  // Filter out don't-care coverage
            primeMap.put( prime, baseMap );
        }

        // === PHASE 2: Identify Essential Primes ===
        List<String> keyPrimes = new ArrayList<>( );
        Set<Integer> coverMap = new HashSet<>( );

        boolean changed;
        do {
            changed = false;
            Map<Integer, List<String>> termMap = new HashMap<>( );

            // Build coverage map for uncovered terms
            for ( String prime : primes ) {
                if ( keyPrimes.contains( prime ) ) continue;

                for ( int term : primeMap.get( prime ) ) {
                    if ( !coverMap.contains( term ) ) {
                        termMap.computeIfAbsent( term, k -> new ArrayList<>( ) ).add( prime );
                    }
                }
            }

            // Add primes that uniquely cover any term
            for ( Map.Entry<Integer, List<String>> entry : termMap.entrySet( ) ) {
                if ( entry.getValue( ).size( ) == 1 ) {
                    String essential = entry.getValue( ).get( 0 );
                    if ( !keyPrimes.contains( essential ) ) {
                        keyPrimes.add( essential );
                        coverMap.addAll( primeMap.get( essential ) );
                        changed = true;
                    }
                }
            }
        } while ( changed );

        // === PHASE 3: Check for Complete Coverage ===
        if ( coverMap.containsAll( terms ) ) {
            return keyPrimes;
        }

        // === PHASE 4: Petrick's Method for Remaining Terms ===
        List<String> nonKey = new ArrayList<>( primes );
        nonKey.removeAll( keyPrimes );

        // Generate solution set candidates in order of increasing size
        Queue<List<String>> setQueue = new PriorityQueue<>( Comparator.comparingInt( List::size ) );
        setQueue.add( new ArrayList<>( keyPrimes ) );

        while ( !setQueue.isEmpty( ) ) {
            List<String> trialSet = setQueue.poll( );

            // Check if current candidate covers all terms
            if ( coversAll( trialSet, primeMap, terms ) ) {
                return trialSet;
            }

            // Generate new candidates by adding one non-essential prime
            for ( String prime : nonKey ) {
                if ( !trialSet.contains( prime ) ) {
                    List<String> set = new ArrayList<>( trialSet );
                    set.add( prime );
                    setQueue.add( set );
                }
            }
        }

        return keyPrimes;  // Fallback if no solution found (shouldn't happen for valid inputs)
    }

    private boolean coversAll ( List<String> primes, Map<String, Set<Integer>> coverage, Set<Integer> terms ) {
        Set<Integer> covered = new HashSet<>( );
        for ( String prime : primes ) {
            covered.addAll( coverage.get( prime ) );
        }
        return covered.containsAll( terms );
    }

    private Set<Integer> cover ( String term ) {
        Set<Integer> covered = new HashSet<>( );
        List<String> expansions = expandTerm( term );

        for ( String expanded : expansions ) {
            covered.add( toDec( expanded ) );
        }

        return covered;
    }

    private List<String> expandTerm ( String term ) {
        List<String> expansions = new ArrayList<>( );
        expand( term, 0, expansions );
        return expansions;
    }

    private void expand ( String term, int index, List<String> expansions ) {
        if ( index == term.length( ) ) {
            expansions.add( term );
            return;
        }

        if ( term.charAt( index ) == '-' ) {
            StringBuilder sb0 = new StringBuilder( term );
            sb0.setCharAt( index, '0' );
            expand( sb0.toString( ), index + 1, expansions );

            StringBuilder sb1 = new StringBuilder( term );
            sb1.setCharAt( index, '1' );
            expand( sb1.toString( ), index + 1, expansions );
        } else {
            expand( term, index + 1, expansions );
        }
    }

    private List<List<String>> groupByOnes ( List<String> terms ) {
        List<List<String>> groups = new ArrayList<>( );
        for ( int i = 0; i <= vars; i++ ) {
            groups.add( new ArrayList<>( ) );
        }

        for ( String term : terms ) {
            int count = countOnes( term );
            groups.get( count ).add( term );
        }

        return groups;
    }

    private int countOnes ( String term ) {
        int count = 0;
        for ( char c : term.toCharArray( ) ) {
            if ( c == '1' ) count++;
        }
        return count;
    }

    private String combineTerms ( String term1, String term2 ) {
        int diff = -1;
        for ( int i = 0; i < term1.length( ); i++ ) {
            if ( term1.charAt( i ) != term2.charAt( i ) ) {
                if ( diff != -1 ) return null; // More than one difference
                diff = i;
            }
        }

        if ( diff == -1 ) return null; // Terms are identical

        return term1.substring( 0, diff ) + "-" + term1.substring( diff + 1 );
    }

    private String toBin ( int n ) {
        StringBuilder bin = new StringBuilder( Integer.toBinaryString( n ) );
        while ( bin.length( ) < vars ) {
            bin.insert( 0, "0" );
        }
        return bin.toString( );
    }

    private int toDec ( String bin ) {
        return Integer.parseInt( bin, 2 );
    }

    private String toAlgebraic ( String term ) {
        StringBuilder sb = new StringBuilder( );
        char var = 'A';

        for ( int i = 0; i < term.length( ); i++ ) {
            char c = term.charAt( i );
            if ( c == '0' ) {
                sb.append( var ).append( "'" );
            } else if ( c == '1' ) {
                sb.append( var );
            }
            var++;
        }

        return sb.toString( );
    }

    public String getSOP ( ) {
        if ( keyPrimes.isEmpty( ) ) {
            return minterms.isEmpty( ) ? "0" : "1";
        }

        return keyPrimes.stream( )
                .map( this::toAlgebraic )
                .collect( Collectors.joining( " + " ) );
    }

    public String getPOS ( ) {
        KMapSolver complementSolver = new KMapSolver( vars, minterms, dontCares );
        String complementSOP = complementSolver.getSOP( );

        return applyDeMorgans( complementSOP );
    }

    private String applyDeMorgans ( String sop ) {
        if ( sop.equals( "0" ) ) return "1";
        if ( sop.equals( "1" ) ) return "0";

        // Split into sum terms
        String[] sums = sop.split( " \\+ " );
        List<String> sumTerms = new ArrayList<>( );

        for ( String sum : sums ) {
            StringBuilder product = new StringBuilder( "(" );
            boolean flag = true;

            // Process each literal in the product term
            for ( int i = 0; i < sum.length( ); i++ ) {
                char c = sum.charAt( i );
                if ( c == '\'' ) continue;

                if ( !flag ) {
                    product.append( " + " );
                }
                flag = false;

                // Principle of Duality
                if ( i + 1 < sum.length( ) && sum.charAt( i + 1 ) == '\'' ) {
                    // Was complemented, now uncomplement (Involution)
                    product.append( c );
                    i++; // Skip the '
                } else {
                    // Was uncomplemented, now complement
                    product.append( c ).append( "'" );
                }
            }
            product.append( ")" );
            sumTerms.add( product.toString( ) );
        }

        return String.join( " ", sumTerms );
    }

    public Set<Integer> getMinterms ( ) {
        return minterms;
    }

    public Set<Integer> getDontCares ( ) {
        return dontCares;
    }

    public List<String> getImplicants ( ) {
        return keyPrimes;
    }
}