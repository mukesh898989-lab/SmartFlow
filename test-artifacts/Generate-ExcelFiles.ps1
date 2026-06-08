# =====================================================================
#  SmartFlowApp -- Test Artifact Workbook Generator
# =====================================================================
#  Generates ONE Excel workbook in this folder:
#
#     SmartFlowApp_TestArtifacts.xlsx
#         |-- Sheet 1: Test Scenarios
#         |-- Sheet 2: Test Cases
#         |-- Sheet 3: RTM
#
#  Requires MS Excel installed on Windows.
#  Run:   powershell -ExecutionPolicy Bypass -File .\Generate-ExcelFiles.ps1
# =====================================================================

$ErrorActionPreference = 'Stop'

$OutDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$OutFile = Join-Path $OutDir "SmartFlowApp_TestArtifacts.xlsx"

# --- Start Excel (hidden) -------------------------------------------
$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$excel.DisplayAlerts = $false

# Color constants
$HeaderColor   = 4626167   # dark blue
$HeaderText    = 16777215  # white
$PassColor     = 13434828  # light green
$FailColor     = 13428479  # light red/peach
$NegativeColor = 13948116  # light yellow

# ---------- Helper: write a 2D array to a worksheet ----------
function Write-Sheet {
    param(
        [object]$Worksheet,
        [string[]]$Headers,
        [object[][]]$Rows,
        [string]$Title
    )

    $Worksheet.Cells.Item(1,1) = $Title
    $titleRange = $Worksheet.Range($Worksheet.Cells.Item(1,1), $Worksheet.Cells.Item(1, $Headers.Length))
    $titleRange.Merge() | Out-Null
    $titleRange.Font.Bold = $true
    $titleRange.Font.Size = 14
    $titleRange.HorizontalAlignment = -4108
    $titleRange.Interior.Color = $HeaderColor
    $titleRange.Font.Color = $HeaderText

    for ($c = 0; $c -lt $Headers.Length; $c++) {
        $cell = $Worksheet.Cells.Item(2, $c + 1)
        $cell.Value2 = $Headers[$c]
        $cell.Font.Bold = $true
        $cell.Interior.Color = $HeaderColor
        $cell.Font.Color = $HeaderText
        $cell.HorizontalAlignment = -4108
    }

    for ($r = 0; $r -lt $Rows.Length; $r++) {
        $row = $Rows[$r]
        for ($c = 0; $c -lt $row.Length; $c++) {
            $cell = $Worksheet.Cells.Item($r + 3, $c + 1)
            $cell.Value2 = $row[$c]
            $cell.VerticalAlignment = -4160
            $cell.WrapText = $true
        }
    }

    $lastRow = 2 + $Rows.Length
    $tableRange = $Worksheet.Range($Worksheet.Cells.Item(2,1), $Worksheet.Cells.Item($lastRow, $Headers.Length))
    $tableRange.Borders.LineStyle = 1
    $Worksheet.Columns.AutoFit() | Out-Null
    $Worksheet.Rows.AutoFit() | Out-Null
    $Worksheet.Rows.Item(1).RowHeight = 28

    $Worksheet.Activate()
    $Worksheet.Application.ActiveWindow.SplitRow = 2
    $Worksheet.Application.ActiveWindow.FreezePanes = $true
}

# ---------- Helper: color a column conditionally ----------
function Color-StatusColumn {
    param([object]$Worksheet, [int]$Col, [int]$FirstRow, [int]$LastRow)
    for ($r = $FirstRow; $r -le $LastRow; $r++) {
        $val = "$($Worksheet.Cells.Item($r, $Col).Value2)"
        if ($val -match 'Pass') {
            $Worksheet.Cells.Item($r, $Col).Interior.Color = $PassColor
        } elseif ($val -match 'Fail') {
            $Worksheet.Cells.Item($r, $Col).Interior.Color = $FailColor
        } elseif ($val -match 'Negative') {
            $Worksheet.Cells.Item($r, $Col).Interior.Color = $NegativeColor
        }
    }
}


# =====================================================================
#  Create the workbook with 3 sheets (in order: Scenarios, Cases, RTM)
# =====================================================================
# Force Excel to create new workbooks with exactly 1 sheet
$prevSheets = $excel.SheetsInNewWorkbook
$excel.SheetsInNewWorkbook = 1
$wb = $excel.Workbooks.Add()
$excel.SheetsInNewWorkbook = $prevSheets

# Sheet 1 is already there -- rename it
$wsScenarios = $wb.Sheets.Item(1)
$wsScenarios.Name = "Test Scenarios"

# Add Sheet 2 AFTER Test Scenarios
$wsCases = $wb.Sheets.Add([System.Reflection.Missing]::Value, $wsScenarios)
$wsCases.Name = "Test Cases"

# Add Sheet 3 AFTER Test Cases
$wsRTM = $wb.Sheets.Add([System.Reflection.Missing]::Value, $wsCases)
$wsRTM.Name = "RTM"


# =====================================================================
#  SHEET 1 -- Test Scenarios
# =====================================================================
$scenarioHeaders = @("Scenario ID","Module","Scenario Description","Priority","Type")
$scenarioRows = @(
    ,@("TS_01","Admin",        "Admin can log in with valid credentials and reach the Queue Overview dashboard","High","Positive")
    ,@("TS_02","Admin",        "Admin can create a new Department","High","Positive")
    ,@("TS_03","Admin",        "Admin can create a new Doctor under a Department","High","Positive")
    ,@("TS_04","Admin",        "Admin can create a Receptionist staff account","High","Positive")
    ,@("TS_05","Admin",        "Admin can log out and is redirected to /auth/login","Medium","Positive")
    ,@("TS_06","Receptionist", "Receptionist can log in with the account created by Admin","High","Positive")
    ,@("TS_07","Receptionist", "Receptionist can register a new Patient","High","Positive")
    ,@("TS_08","Receptionist", "Receptionist can generate a Token for a Patient + Doctor","High","Positive")
    ,@("TS_09","Receptionist", "Receptionist can log out","Medium","Positive")
    ,@("TS_10","Doctor",       "Doctor can log in and see the patient waiting in My Queue","High","Positive")
    ,@("TS_11","Doctor",       "Doctor can Call First Patient -- status moves to IN_PROGRESS","High","Positive")
    ,@("TS_12","Doctor",       "Doctor can Complete & Done a consultation -- status moves to COMPLETED","High","Positive")
    ,@("TS_13","Patient",      "Patient can track a completed token and see 'Consultation Completed'","High","Positive")
    ,@("TS_14","Doctor",       "Doctor can log out","Medium","Positive")
    ,@("TS_15","Patient",      "Walk-in Patient can self-register on the public /patient/register form","High","Positive")
    ,@("TS_16","Patient",      "Self-registered Patient can pick a Department + Doctor and receive a token","High","Positive")
    ,@("TS_17","Security",     "System must reject login attempts with INVALID password","High","Negative")
    ,@("TS_18","Security",     "System must reject login attempts with EMPTY credentials","High","Negative")
    ,@("TS_19","Patient",      "System must handle tracking of a NON-EXISTENT token gracefully","High","Negative")
)
Write-Sheet -Worksheet $wsScenarios -Headers $scenarioHeaders -Rows $scenarioRows -Title "SmartFlowApp -- Test Scenarios"
Color-StatusColumn -Worksheet $wsScenarios -Col 5 -FirstRow 3 -LastRow (2 + $scenarioRows.Length)
$wsScenarios.Columns.Item(3).ColumnWidth = 55


# =====================================================================
#  SHEET 2 -- Test Cases
# =====================================================================
$tcHeaders = @("TC ID","Scenario ID","Test Case Description","Pre-Conditions","Test Steps","Expected Result","Actual Result","Status","Type")
$tcRows = @(
    ,@("TC01","TS_01","Admin login with valid credentials",
        "Backend + Frontend are up. Default admin seeded.",
        "1. Open /auth/login`n2. Enter admin@hospital.com / Admin@123`n3. Click Sign In",
        "URL changes to /admin/* and Queue Overview dashboard appears",
        "Admin dashboard rendered as expected","Passed","Positive")

    ,@("TC02","TS_02","Admin creates Oncology department",
        "Admin is logged in.",
        "1. Go to Departments`n2. Enter unique department name + description`n3. Click Create",
        "Success alert appears and the new department shows in the table",
        "Department created and listed","Passed","Positive")

    ,@("TC03","TS_03","Admin creates a new doctor",
        "Department from TC02 exists.",
        "1. Go to Doctors`n2. Fill name, email, password, specialization, department`n3. Click Create",
        "Success alert appears and doctor row shows in the table",
        "Doctor created and listed","Passed","Positive")

    ,@("TC04","TS_04","Admin creates a receptionist user",
        "Admin is logged in.",
        "1. Go to Users`n2. Fill name, email, password, role=Receptionist`n3. Click Create",
        "Success alert appears and the receptionist row shows in the staff table",
        "Receptionist created and listed","Passed","Positive")

    ,@("TC05","TS_05","Admin logout",
        "Admin is logged in.",
        "1. Click Logout in the admin navbar",
        "Browser redirects to /auth/login and email field is visible",
        "Logout successful","Passed","Positive")

    ,@("TC06","TS_06","Receptionist login",
        "Receptionist account from TC04 exists.",
        "1. Open /auth/login`n2. Enter receptionist credentials`n3. Click Sign In",
        "Receptionist portal opens with Register Patient + Generate Token nav links",
        "Receptionist portal opened","Passed","Positive")

    ,@("TC07","TS_07","Receptionist registers a patient",
        "Receptionist is logged in.",
        "1. Go to Register Patient`n2. Fill name, phone, email, age, gender`n3. Submit",
        "Success alert + quick-link to Generate Token visible",
        "Patient registered","Passed","Positive")

    ,@("TC08","TS_08","Receptionist generates a token",
        "Patient from TC07 exists. Doctor + Department exist.",
        "1. Click Generate Token quick-link`n2. Select Patient, Department, Doctor, Normal priority`n3. Submit",
        "Token number is displayed and captured for later use",
        "Token generated","Passed","Positive")

    ,@("TC09","TS_09","Receptionist logout",
        "Receptionist is logged in.",
        "1. Click Logout",
        "Browser redirects to /auth/login",
        "Logout successful","Passed","Positive")

    ,@("TC10","TS_10","Doctor login and view queue",
        "Doctor account exists. Token from TC08 is WAITING.",
        "1. Login as the doctor`n2. Open My Queue",
        "Patient appears as a row in the waiting queue",
        "Patient visible in queue","Passed","Positive")

    ,@("TC11","TS_11","Doctor calls first patient",
        "Doctor is logged in and queue has at least one patient.",
        "1. Click Call First Patient",
        "Patient moves to Current Patient card with token + Complete & Done button",
        "Patient is IN_PROGRESS","Passed","Positive")

    ,@("TC12","TS_12","Doctor completes consultation",
        "A patient is currently IN_PROGRESS.",
        "1. Click Complete & Done",
        "Current patient cleared and waiting queue shows empty messages",
        "Consultation COMPLETED","Passed","Positive")

    ,@("TC13","TS_13","Patient tracks completed token",
        "Token from TC08 is now COMPLETED.",
        "1. Open /patient/track`n2. Enter token number`n3. Click Track",
        "'Consultation Completed' + 'Thank you for visiting' messages visible",
        "Completed status shown","Passed","Positive")

    ,@("TC14","TS_14","Doctor logout",
        "Doctor is logged in.",
        "1. Click Logout",
        "Browser redirects to /auth/login",
        "Logout successful","Passed","Positive")

    ,@("PFTC04","TS_15","Patient self-registers via /patient/register",
        "Department + Doctor seeded by admin setup.",
        "1. Open /patient`n2. Click Register`n3. Fill personal details`n4. Continue",
        "Patient remains on /patient/register and proceeds to step 2",
        "Self-registration step 1 complete","Passed","Positive")

    ,@("PFTC05","TS_16","Patient picks doctor and gets token",
        "Self-registered patient details entered.",
        "1. Select Department`n2. Select Doctor`n3. Submit",
        "Token number displayed and Track My Queue Position link shown",
        "Token issued to self-registered patient","Passed","Positive")

    ,@("TC_FAIL_01","TS_17","[NEGATIVE] Invalid admin login",
        "Backend + Frontend are up. Default admin seeded.",
        "1. Open /auth/login`n2. Enter admin@hospital.com / WrongPassword@123`n3. Click Sign In`n4. Assert URL contains /admin",
        "Test ASSERTS user reaches /admin -- expected to FAIL because backend correctly rejects invalid credentials",
        "AssertionError: URL was still /auth/login. Backend correctly rejected the invalid credentials. Test fails as designed.",
        "Failed (Demo)","Negative")

    ,@("TC_FAIL_02","TS_18","[NEGATIVE] Empty credentials submit",
        "Backend + Frontend are up.",
        "1. Open /auth/login`n2. Click Sign In with empty email + password`n3. Assert URL contains /admin",
        "Test ASSERTS user reaches /admin -- expected to FAIL because Angular form validation correctly blocks submission",
        "AssertionError: URL was still /auth/login. Form validation prevented submit. Test fails as designed.",
        "Failed (Demo)","Negative")

    ,@("TC_FAIL_03","TS_19","[NEGATIVE] Track non-existent token",
        "Backend + Frontend are up.",
        "1. Open /patient/track`n2. Enter INVALID-TOKEN-9999`n3. Click Track`n4. Assert 'Consultation Completed' visible",
        "Test ASSERTS completion banner -- expected to FAIL because token does not exist in DB",
        "TimeoutException: 'Consultation Completed' element never appeared. System correctly handles unknown tokens. Test fails as designed.",
        "Failed (Demo)","Negative")
)
Write-Sheet -Worksheet $wsCases -Headers $tcHeaders -Rows $tcRows -Title "SmartFlowApp -- Test Cases"
Color-StatusColumn -Worksheet $wsCases -Col 8 -FirstRow 3 -LastRow (2 + $tcRows.Length)
Color-StatusColumn -Worksheet $wsCases -Col 9 -FirstRow 3 -LastRow (2 + $tcRows.Length)
$wsCases.Columns.Item(3).ColumnWidth = 35
$wsCases.Columns.Item(4).ColumnWidth = 25
$wsCases.Columns.Item(5).ColumnWidth = 40
$wsCases.Columns.Item(6).ColumnWidth = 40
$wsCases.Columns.Item(7).ColumnWidth = 40


# =====================================================================
#  SHEET 3 -- RTM
# =====================================================================
$rtmHeaders = @("Req ID","Requirement Description","Module","Scenario ID(s)","Test Case ID(s)","Status")
$rtmRows = @(
    ,@("REQ-001","Admin must be able to log in with valid credentials","Auth/Admin","TS_01","TC01","Passed")
    ,@("REQ-002","Admin must be able to create departments","Admin","TS_02","TC02","Passed")
    ,@("REQ-003","Admin must be able to create doctors under a department","Admin","TS_03","TC03","Passed")
    ,@("REQ-004","Admin must be able to create staff (receptionist) accounts","Admin","TS_04","TC04","Passed")
    ,@("REQ-005","Receptionist must be able to register a new patient","Receptionist","TS_07","TC07","Passed")
    ,@("REQ-006","Receptionist must be able to generate a token for a patient","Receptionist","TS_08","TC08","Passed")
    ,@("REQ-007","Doctor must see assigned patients in My Queue","Doctor","TS_10","TC10","Passed")
    ,@("REQ-008","Doctor must be able to call a patient (status -> IN_PROGRESS)","Doctor","TS_11","TC11","Passed")
    ,@("REQ-009","Doctor must be able to complete a consultation (status -> COMPLETED)","Doctor","TS_12","TC12","Passed")
    ,@("REQ-010","Patient must be able to track token status using the token number","Patient","TS_13","TC13","Passed")
    ,@("REQ-011","Walk-in patients must be able to self-register via public form","Patient","TS_15, TS_16","PFTC04, PFTC05","Passed")
    ,@("REQ-012","All roles must be able to log out securely","Auth","TS_05, TS_09, TS_14","TC05, TC09, TC14","Passed")
    ,@("REQ-013","System must reject login attempts with INVALID credentials","Security","TS_17","TC_FAIL_01","Failed (Demo)")
    ,@("REQ-014","System must reject login submissions with EMPTY fields","Security","TS_18","TC_FAIL_02","Failed (Demo)")
    ,@("REQ-015","System must handle tracking of NON-EXISTENT tokens gracefully","Patient","TS_19","TC_FAIL_03","Failed (Demo)")
)
Write-Sheet -Worksheet $wsRTM -Headers $rtmHeaders -Rows $rtmRows -Title "SmartFlowApp -- Requirements Traceability Matrix (RTM)"
Color-StatusColumn -Worksheet $wsRTM -Col 6 -FirstRow 3 -LastRow (2 + $rtmRows.Length)
$wsRTM.Columns.Item(2).ColumnWidth = 55


# Activate the first sheet so the file opens on Test Scenarios
$wsScenarios.Activate()

# Save workbook
if (Test-Path $OutFile) { Remove-Item $OutFile -Force }
$wb.SaveAs($OutFile, 51)
$wb.Close()
Write-Host "Wrote $OutFile"

# --- Cleanup ---------------------------------------------------------
$excel.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) | Out-Null
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()

Write-Host "`nWorkbook generated successfully."
