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
        SELECT stock
        FROM inventario
        WHERE producto='Paracetamol'
    """)

    stock_actual = cur_a.fetchone()[0]

    if stock_actual < cantidad:
        raise Exception("Stock insuficiente en Arequipa")

    cur_a.execute("""
        UPDATE inventario
        SET stock = stock - %s
        WHERE producto='Paracetamol'
    """, (cantidad,))

    cur_l.execute("""
        UPDATE inventario
        SET stock = stock + %s
        WHERE producto='Paracetamol'
    """, (cantidad,))

    conn_arequipa.commit()
    conn_lima.commit()

    print("Transferencia realizada correctamente")

except Exception as e:

    conn_arequipa.rollback()
    conn_lima.rollback()

    print("Error:", e)

finally:

    cur_a.close()
    cur_l.close()

    conn_arequipa.close()
    conn_lima.close()