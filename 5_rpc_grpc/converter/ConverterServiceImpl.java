package converter;

import io.grpc.stub.StreamObserver;

public class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase {

  @Override
  public void convert(ConvertRequest req, StreamObserver<ConvertResponse> responseObserver) {

    double value = req.getValue();
    String type = req.getType();

    double result = 0;

    // Validación
    if (value < 0) {

      responseObserver.onNext(
          ConvertResponse.newBuilder().setResult(0).setMessage("Valor inválido").build());

      responseObserver.onCompleted();
      return;
    }

    switch (type) {

      // Conversiones pedidas
      case "c_f":
        result = (value * 1.8) + 32;
        break;

      case "soles_dolares":
        result = value / 3.7;
        break;

      case "km_millas":
        result = value * 0.621371;
        break;

      // Conversiones extras
      case "kg_lb":
        result = value * 2.20462;
        break;

      case "m_cm":
        result = value * 100;
        break;

      case "h_min":
        result = value * 60;
        break;

      default:
        responseObserver.onNext(
            ConvertResponse.newBuilder().setResult(0).setMessage("Conversión inválida").build());

        responseObserver.onCompleted();

        return;
    }

    // Logs servidor
    System.out.println("Conversión: " + type);

    System.out.println("Valor recibido: " + value);

    System.out.println("Resultado enviado: " + result);

    ConvertResponse response =
        ConvertResponse.newBuilder().setResult(result).setMessage("Conversión exitosa").build();

    responseObserver.onNext(response);

    responseObserver.onCompleted();
  }
}
