import psycopg2

try:
    conn_arequipa = psycopg2.connect(
        host="localhost",
        database="almacen_arequipa",
        user="postgres",
        password="postgres"
    )

    conn_lima = psycopg2.connect(
        host="localhost",
        database="almacen_lima",
        user="postgres",
        password="postgres"
    )

    conn_arequipa.autocommit = False
    conn_lima.autocommit = False

    cur_a = conn_arequipa.cursor()
    cur_l = conn_lima.cursor()

    cantidad = 20

    cur_a.execute("""
        UPDATE inventario
        SET stock = stock - %s
        WHERE producto='Paracetamol'
    """, (cantidad,))

    print("Stock descontado en Arequipa")

    raise Exception("Nodo Lima fuera de servicio")

    cur_l.execute("""
        UPDATE inventario
        SET stock = stock + %s
        WHERE producto='Paracetamol'
    """, (cantidad,))

    conn_arequipa.commit()
    conn_lima.commit()

except Exception as e:

    print("Error detectado:", e)

    conn_arequipa.rollback()
    conn_lima.rollback()

    print("Rollback ejecutado correctamente")

finally:

    cur_a.close()
    cur_l.close()

    conn_arequipa.close()
    conn_lima.close()