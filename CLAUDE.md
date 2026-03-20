# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NetXMS fork of Eclipse SWTChart library (v1.3.10) - a Java charting library for SWT/RWT applications. Provides line, bar, pie, and doughnut charts. Fork adds extended legend functionality, inverted chart support, correct label formatting with unit multipliers, and stacked chart improvements.

## Build Commands

```bash
mvn clean package              # Build both modules
mvn clean compile              # Compile only
mvn javadoc:jar                # Generate JavaDoc
mvn deploy -Pcentral           # Deploy to Maven Central
```

**Platform profiles** (auto-activated based on OS):
- `windows`, `linux-x86_64`, `linux-aarch64`, `macos-x86_64`, `macos-aarch64`

## Architecture

### Module Structure

- **swtchart/** - Desktop SWT version for Eclipse/RCP applications
- **rwtchart/** - Web version using Eclipse RAP/RWT (parallel implementation)

Both modules share the same package structure (`org.eclipse.swtchart`) but target different widget toolkits.

### Core Design

**Chart** extends SWT `Composite` and contains:
- **Title** (ITitle) - Chart title
- **Legend** (ILegend) - Series legend
- **AxisSet** (IAxisSet) - X and Y axes container
- **PlotArea** (IPlotArea) - Main drawing area
- **SeriesSet** (ISeriesSet) - Data series container

### Package Organization

```
org.eclipse.swtchart/
├── I*.java              # Public API interfaces (IAxis, ISeries, ILegend, etc.)
├── Chart.java           # Main chart widget
├── model/               # Data models (CartesianSeriesModel, DoubleArraySeriesModel, etc.)
└── internal/            # Implementation classes (not public API)
    ├── axis/            # Axis, AxisSet, AxisTick implementations
    ├── series/          # Series, BarSeries, LineSeries, CircularSeries
    └── compress/        # Data compression for large datasets
```

### Series Types

Via `ISeries.SeriesType` enum:
- **LINE** - Line charts (with optional stacking)
- **BAR** - Bar charts (with optional stacking)
- **PIE** - Pie charts
- **DOUGHNUT** - Doughnut/ring charts

### Key Patterns

- Public interfaces (I-prefix) in root package define API contract
- `internal/` package contains all implementations
- Data models abstract data binding (supports numeric arrays, dates, categories)
- Compression layer (`compress/`) optimizes rendering of large datasets
