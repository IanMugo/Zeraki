# Billing Application API Test Script
# Run this in PowerShell to test the API endpoints

$baseUrl = "http://localhost:8080/api"

Write-Host "=== Testing Billing Application API ===" -ForegroundColor Green
Write-Host ""

# Test 1: Create a Customer
Write-Host "1. Creating a customer..." -ForegroundColor Yellow
$customer = @{
    name = "John Doe"
    email = "john.doe@example.com"
    phone = "123-456-7890"
} | ConvertTo-Json

$createCustomer = Invoke-RestMethod -Uri "$baseUrl/customers" -Method Post -Body $customer -ContentType "application/json"
Write-Host "   Created Customer ID: $($createCustomer.id)" -ForegroundColor Cyan
$customerId = $createCustomer.id
Write-Host ""

# Test 2: Get All Customers
Write-Host "2. Getting all customers..." -ForegroundColor Yellow
$customers = Invoke-RestMethod -Uri "$baseUrl/customers" -Method Get
Write-Host "   Found $($customers.Count) customer(s)" -ForegroundColor Cyan
Write-Host ""

# Test 3: Create an Invoice
Write-Host "3. Creating an invoice..." -ForegroundColor Yellow
$invoice = @{
    customer = @{ id = $customerId }
    amount = 1000.00
    dueDate = "2026-03-18"
} | ConvertTo-Json

$createInvoice = Invoke-RestMethod -Uri "$baseUrl/invoices" -Method Post -Body $invoice -ContentType "application/json"
Write-Host "   Created Invoice ID: $($createInvoice.id)" -ForegroundColor Cyan
$invoiceId = $createInvoice.id
Write-Host ""

# Test 4: Get All Invoices
Write-Host "4. Getting all invoices..." -ForegroundColor Yellow
$invoices = Invoke-RestMethod -Uri "$baseUrl/invoices" -Method Get
Write-Host "   Found $($invoices.Count) invoice(s)" -ForegroundColor Cyan
Write-Host ""

# Test 5: Create a Payment
Write-Host "5. Creating a payment..." -ForegroundColor Yellow
$payment = @{
    invoice = @{ id = $invoiceId }
    amount = 500.00
    paymentDate = "2026-02-18"
    paymentMethod = "Credit Card"
    transactionNumber = "TXN-$(Get-Random)"
} | ConvertTo-Json

$createPayment = Invoke-RestMethod -Uri "$baseUrl/payments" -Method Post -Body $payment -ContentType "application/json"
Write-Host "   Created Payment ID: $($createPayment.id)" -ForegroundColor Cyan
Write-Host ""

# Test 6: Get Dashboard Summary
Write-Host "6. Getting dashboard summary..." -ForegroundColor Yellow
$summary = Invoke-RestMethod -Uri "$baseUrl/dashboard/summary" -Method Get
Write-Host "   Summary:" -ForegroundColor Cyan
$summary | Format-List
Write-Host ""

# Test 7: Get Top Customers
Write-Host "7. Getting top customers..." -ForegroundColor Yellow
$topCustomers = Invoke-RestMethod -Uri "$baseUrl/dashboard/top-customers" -Method Get
Write-Host "   Top Customers:" -ForegroundColor Cyan
$topCustomers | Format-Table
Write-Host ""

Write-Host "=== API Tests Complete ===" -ForegroundColor Green
