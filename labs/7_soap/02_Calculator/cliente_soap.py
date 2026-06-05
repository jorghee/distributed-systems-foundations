from zeep import Client

# Url del servicio SOAP de la calculadora
url_wsdl = "http://www.dneonline.com/calculator.asmx?WSDL"

client = Client(url_wsdl)
resultado = client.service.Add(5, 8)

print("Resultado de la suma:", resultado)

