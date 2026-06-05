from zeep import Client

wsdlUrl = "http://www.dneonline.com/calculator.asmx?WSDL"

client = Client(wsdlUrl)

print("=== CLIENTE SOAP BÁSICO ===")
print("WSDL usado:", wsdlUrl)

print("\n=== INFORMACIÓN DEL SERVICIO ===")

for serviceName, serviceData in client.wsdl.services.items():
    print("Servicio encontrado:", serviceName)

    print("\nPuertos disponibles:")
    for portName, portData in serviceData.ports.items():
        if portName == "CalculatorSoap":
            print("- CalculatorSoap: SOAP 1.1")
        elif portName == "CalculatorSoap12":
            print("- CalculatorSoap12: SOAP 1.2")
        else:
            print("-", portName)

print("\n=== MÉTODOS DISPONIBLES ===")

mainService = client.wsdl.services["Calculator"]
mainPort = mainService.ports["CalculatorSoap"]

for methodName in mainPort.binding._operations:
    print("-", methodName)

print("\n=== CONSUMO DEL SERVICIO SOAP ===")

firstNumber = 20
secondNumber = 7

soapService = client.bind("Calculator", "CalculatorSoap")

result = soapService.Subtract(firstNumber, secondNumber)

print("Puerto usado: CalculatorSoap")
print("Versión usada: SOAP 1.1")
print("Método usado: Subtract")
print(f"Primer número: {firstNumber} | Segundo número: {secondNumber} | Resultado: {result}")