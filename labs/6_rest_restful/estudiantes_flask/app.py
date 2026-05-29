import re

from flask import Flask, jsonify, request, send_from_directory

app = Flask(__name__, static_folder="static")

estudiantes = [
    {
        "id": 1,
        "codigo": "20240001",
        "nombre": "Ana Torres",
        "correo": "ana.torres@unsa.edu.pe",
        "carrera": "Ingenieria de Sistemas"
    }
]

siguiente_id = 2

patron_correo = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def buscar_estudiante(id):
    for estudiante in estudiantes:
        if estudiante["id"] == id:
            return estudiante
    return None


def codigo_repetido(codigo, id_actual=None):
    for estudiante in estudiantes:
        if estudiante["codigo"] == codigo and estudiante["id"] != id_actual:
            return True
    return False


def validar_estudiante(datos, id_actual=None):
    codigo = str(datos.get("codigo", "")).strip()
    nombre = str(datos.get("nombre", "")).strip()
    correo = str(datos.get("correo", "")).strip()
    carrera = str(datos.get("carrera", "")).strip()

    errores = []

    if not codigo:
        errores.append("El codigo es obligatorio")
    elif not codigo.isdigit() or len(codigo) < 6:
        errores.append("El codigo debe tener solo numeros y minimo 6 digitos")
    elif codigo_repetido(codigo, id_actual):
        errores.append("El codigo ya esta registrado")

    if not nombre:
        errores.append("El nombre es obligatorio")
    elif len(nombre) < 3:
        errores.append("El nombre debe tener minimo 3 caracteres")

    if correo and not patron_correo.match(correo):
        errores.append("El correo no tiene un formato valido")

    if not carrera:
        errores.append("La carrera es obligatoria")

    estudiante = {
        "codigo": codigo,
        "nombre": nombre,
        "correo": correo,
        "carrera": carrera
    }

    return errores, estudiante


@app.get("/")
def inicio():
    return send_from_directory(app.static_folder, "index.html")


@app.get("/estudiantes")
def listar_estudiantes():
    return jsonify(estudiantes)


@app.post("/estudiantes")
def registrar_estudiante():
    global siguiente_id

    datos = request.get_json(silent=True)
    if not datos:
        return jsonify({"error": "Datos invalidos"}), 400

    errores, estudiante = validar_estudiante(datos)
    if errores:
        return jsonify({"errores": errores}), 400

    estudiante["id"] = siguiente_id

    estudiantes.append(estudiante)
    siguiente_id += 1

    return jsonify(estudiante), 201


@app.put("/estudiantes/<int:id>")
def actualizar_estudiante(id):
    datos = request.get_json(silent=True)
    if not datos:
        return jsonify({"error": "Datos invalidos"}), 400

    estudiante = buscar_estudiante(id)
    if not estudiante:
        return jsonify({"error": "Estudiante no encontrado"}), 404

    datos_actualizados = {
        "codigo": datos.get("codigo", estudiante["codigo"]),
        "nombre": datos.get("nombre", estudiante["nombre"]),
        "correo": datos.get("correo", estudiante["correo"]),
        "carrera": datos.get("carrera", estudiante["carrera"])
    }

    errores, datos_limpios = validar_estudiante(datos_actualizados, id)
    if errores:
        return jsonify({"errores": errores}), 400

    estudiante.update(datos_limpios)
    return jsonify(estudiante)


@app.delete("/estudiantes/<int:id>")
def eliminar_estudiante(id):
    estudiante = buscar_estudiante(id)
    if estudiante:
        estudiantes.remove(estudiante)
        return jsonify({"eliminado": True})

    return jsonify({"error": "Estudiante no encontrado"}), 404


if __name__ == "__main__":
    app.run(debug=True)
