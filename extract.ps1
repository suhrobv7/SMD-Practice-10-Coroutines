$src = 'C:/Users/suhro/OneDrive/Rasmlar/Ishchi stol/Practice 10  Coroutines.docx'
$tempDocx = 'C:/Users/suhro/AndroidStudioProjects/Practice10Coroutines/temp.docx'
Copy-Item $src $tempDocx -Force
$dest = 'C:/Users/suhro/AndroidStudioProjects/Practice10Coroutines/extracted_docx'
Add-Type -AssemblyName System.IO.Compression.FileSystem
if (Test-Path $dest) { Remove-Item -Recurse -Force $dest }
[System.IO.Compression.ZipFile]::ExtractToDirectory($tempDocx, $dest)
[xml]$xml = Get-Content "$dest/word/document.xml"
foreach ($node in $xml.DocumentElement.GetElementsByTagName('w:t')) {
    [Console]::WriteLine($node.InnerText)
}
Remove-Item $tempDocx
