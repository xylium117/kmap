# KMap

KMap is an interactive Karnaugh Map solver, Boolean algebra minimizer, and logic circuit prototyping suite. Build maps visually, experiment with minterms and don't-cares, compute exact minimal SOP and POS representations, inspect full truth tables, and synthesize 2-level logic gate implementations in real time.

The project is built entirely in Java with a custom Swing UI engine targeting zero external dependencies and universal cross-platform compatibility.

---
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)&nbsp;
![Swing](https://img.shields.io/badge/GUI-Swing%20%2F%20AWT-5382a1.svg?style=for-the-badge&logo=java&logoColor=white)&nbsp;
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)&nbsp;
[![License: MIT](https://img.shields.io/badge/LICENSE-MIT-green?style=for-the-badge)](https://github.com/xylium117/kmap/blob/master/LICENSE)


## What You Can Explore

- Interactive 2, 3, and 4-variable Karnaugh Map matrix editors
- Click-to-toggle cell state cycling (`0` $\rightarrow$ `1` $\rightarrow$ `X`) with real-time text synchronization
- Exact Boolean minimization using Quine-McCluskey tabular reduction and Petrick's minimal cover algorithm
- Minimal Sum of Products (SOP) and Product of Sums (POS) equations with one-click clipboard copying
- Color-coded rectangular group decompositions with support for wrapped borders (cylindrical, corner, and toroidal topologies)
- Interactive implicant group inspector with hover highlights
- Interactive 2-level logic circuit diagram synthesis:
  - **Basic Gates:** AND, OR, and NOT gate configurations
  - **Universal Logic:** All-NAND and All-NOR equivalent circuit implementations
- Real-time circuit complexity metrics, gate counts, and transformed Boolean expressions
- Comprehensive Truth Table generator with tabular clipboard export
- One-click map operations: preset test cases, grid invert, fill, and reset

## Project Layout

```text
kmap/
├── src/
│   ├── KMapInput.java          Main launcher and interactive matrix configuration
│   ├── KMapInputCell.java      Custom toggleable cell component
│   ├── KMapGUI.java            Solution visualization window and group cards
│   ├── KMapPanel.java          K-Map grid renderer with overlay border engine
│   ├── KMapSolver.java         Quine-McCluskey & Petrick Boolean minimization core
│   ├── LogicDiagramDialog.java Interactive 2-level logic gate schematic renderer
│   ├── TruthTableDialog.java   Full truth table generator and tabular exporter
│   ├── ModernButton.java       Custom anti-aliased UI button component
│   ├── ColorPalette.java       Harmonious color cycle generator for group overlays
│   ├── Colorizer.java          Cell state styling utilities
│   └── FixedWidthPanel.java    Layout alignment helper
├── .github/
│   └── workflows/
│       └── build.yml           Automated JAR build & GitHub Release workflow
├── META-INF/
│   └── MANIFEST.MF             Application entry point manifest
├── build.bat                   Windows compilation and packaging script
├── run.bat                     Windows execution launcher
└── KMap.jar                    Pre-compiled executable JAR (Java 8+ bytecode)
```

## Requirements

- Java Runtime Environment (JRE) or Java Development Kit (JDK) **8 or higher**
- No third-party dependencies or external build tools required

## Run the Application

### Option 1: Direct Execution
Run the pre-packaged executable JAR:

```powershell
java -jar KMap.jar
```

Or on Windows:

```powershell
.\run.bat
```

### Option 2: From Compiled Classes

```powershell
java -cp out src.KMapInput
```

## Build and Package

### Using the Windows Build Script

```powershell
.\build.bat
```

### Manual Compilation and Packaging

Compile all Java sources targeting Java 8 bytecode compatibility:

```powershell
javac --release 8 -d out src/*.java
```

Create the executable JAR:

```powershell
jar --create --file KMap.jar --manifest META-INF/MANIFEST.MF -C out src
```

## Using the Logic Circuit Generator

1. Click **Logic Diagram (Gates)** from the solution window.
2. Select your desired canonical representation:
   - **SOP (Sum of Products)**
   - **POS (Product of Sums)**
3. Choose your gate family:
   - **Basic (AND, OR, NOT):** Direct two-level sum-of-products or product-of-sums realization.
   - **Universal (Only NAND):** De Morgan converted NAND-NAND implementation.
   - **Universal (Only NOR):** De Morgan converted NOR-NOR implementation.
4. Inspect the generated schematic, gate counts, and transformed Boolean formula.
5. Click **Copy Expression** to export the transformed algebraic notation.

## GitHub Actions & Downloadable JAR

An automated CI/CD pipeline is configured in [.github/workflows/build.yml](.github/workflows/build.yml).

### Automated Artifacts
Every push to `master` or `main` automatically compiles the codebase with `--release 8`, tests the packaging, and uploads a fresh `KMap.jar` artifact under the **Actions** tab for instant download.

### Automatic Releases
Pushing a version tag automatically generates a GitHub Release with the standalone executable JAR attached:

```powershell
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

## Development Notes

- Strict Java 8 compatibility is maintained across all components (no Java 9+ only syntax).
- Bytecode is compiled with `--release 8` so that `KMap.jar` executes seamlessly across Java 8 through Java 24+ without `UnsupportedClassVersionError`.
- Group rectangular decomposition supports wrapped cells across rows and columns.
- The circuit schematic renderer uses custom Java2D vector paths and anti-aliasing hints for crisp high-DPI rendering.

## Roadmap

- [ ] 5-variable and 6-variable overlay map support
- [ ] Export circuit diagrams as PNG and SVG vector formats
- [ ] Direct Verilog and VHDL hardware description export
- [ ] Step-by-step Quine-McCluskey Prime Implicant Table visualization
- [ ] Timing diagram and propagation delay simulator

See the [open issues](https://github.com/xylium117/kmap/issues) for a full list of proposed features.

## License

This repository is licensed under the [MIT License](LICENSE). Feel free to use and modify the code as you see fit.

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Make focused changes.
4. Run `build.bat` or `javac --release 8 -d out src/*.java` to ensure clean compilation.
5. Open a pull request with a concise description of your changes.
