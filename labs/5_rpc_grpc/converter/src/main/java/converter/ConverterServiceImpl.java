package converter;

import io.grpc.stub.StreamObserver;

public class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase {

  @Override
  public void convert(ConvertRequest req, StreamObserver<ConvertResponse> responseObserver) {
    double value = req.getValue();
    String type = req.getType();
    double result = 0;
    String message = "Conversión exitosa";

    switch (type) {
      case "c_f":
        result = value * 1.8 + 32;
        break;
      case "soles_dolares":
        if (value < 0) {
          message = "Valor inválido";
        } else {
          result = value / 3.7;
        }
        break;
      case "km_millas":
        if (value < 0) {
          message = "Valor inválido";
        } else {
          result = value * 0.621371;
        }
        break;
      case "kg_lb":
        if (value < 0) {
          message = "Valor inválido";
        } else {
          result = value * 2.20462;
        }
        break;
      case "m_cm":
        if (value < 0) {
          message = "Valor inválido";
        } else {
          result = value * 100;
        }
        break;
      case "h_min":
        if (value < 0) {
          message = "Valor inválido";
        } else {
          result = value * 60;
        }
        break;
      default:
        message = "Conversión inválida";
        break;
    }

    ConvertResponse response =
        ConvertResponse.newBuilder().setResult(result).setMessage(message).build();

    System.out.println("Tipo de conversión recibido: " + type);
    System.out.println("Valor recibido: " + value);
    System.out.println("Resultado enviado: " + result);
    System.out.println("Mensaje de respuesta: " + message);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
