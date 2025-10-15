# Maastricht Bus Scheduling App

**Tech Stack:** JavaFX | MySQL | Google Maps API | A* Algorithm

## Overview

A semester project developed at Maastricht University that provides detailed bus route scheduling for the Maastricht public transit system. Built with **JavaFX** and **GoogleMap API**, powered by the **A\* pathfinding algorithm**, the application achieves **90% accuracy** when compared to Google Maps real-time bus data.

<img width="1192" height="665" alt="Screenshot 2025-10-15 at 21 39 19" src="https://github.com/user-attachments/assets/59e05b90-fcca-40d0-b400-f528d6d4290d" />


## Quick Start

### 1. Configure JavaFX Environment

Set up JavaFX in IntelliJ IDEA to enable the GUI application.

**Troubleshooting "JavaFX runtime components missing" error:**

- Navigate to Run Configurations and add the following VM options:

```
--module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml
```

Replace `path/to/javafx/lib` with your actual JavaFX library path.

### 2. Set Up MySQL Database

Create a new MySQL connection using a database client (we used DBeaver for this project).

**Connection Parameters:**

- **URL:** `jdbc:mysql://localhost:3306/gtfs`
- **Username:** `DACS2024`
- **Password:** `DACS2024`

### 3. Import Required Data

After establishing your database connection:

1. Extract the contents of `data.zip`
2. Import `maas_stops_time.csv` as table **"maas_stops_time"**
3. Import `shapes.csv` as table **"shapes"**

### 4. Launch Application
Run `MapLauncher.java` to start the application.

**Note:** Zipcode data can be found at [`/src/Resources/MassZipLatlon.xlsx`](https://github.com/username/repository-name/blob/main/src/Resources/MassZipLatlon.xlsx)
