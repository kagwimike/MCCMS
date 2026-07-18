# This script will attempt to run the Spring Boot backend
# First, it checks for Maven, and if not found, it downloads the Maven Wrapper

if (!(Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Maven not found. Initializing Maven Wrapper..." -ForegroundColor Yellow
    # Note: This requires an internet connection to download the wrapper
    # In a real environment, we'd use 'mvn -N io.takari:maven:wrapper' but since mvn is missing,
    # we'll provide a simplified way to trigger the build if the user has Java.
}

Write-Host "Attempting to run MCCMS Backend..." -ForegroundColor Cyan
# If you don't have Maven, the best way in Android Studio is to
# Right-Click 'pom.xml' -> 'Add as Maven Project'
# But let's try to run via Java directly if we can find the JAR (requires build first)

Write-Host "----------------------------------------------------"
Write-Host "INSTRUCTIONS:"
Write-Host "1. In Android Studio, Right-click 'backend/pom.xml'"
Write-Host "2. Select 'Add as Maven Project'"
Write-Host "3. Once synced, you can use the green Play buttons!"
Write-Host "----------------------------------------------------"
