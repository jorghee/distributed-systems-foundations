import psycopg2

conn_arequipa = None
conn_lima = None

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

    print("\n=== STOCK INICIAL ===")

    cur_a.execute("SELECT stock FROM inventario WHERE producto='Paracetamol'")
    stock_a = cur_a.fetchone()[0]

    cur_l.execute("SELECT stock FROM inventario WHERE producto='Paracetamol'")
    stock_l = cur_l.fetchone()[0]

    print(f"Arequipa: {stock_a}")
    print(f"Lima: {stock_l}")

    cantidad = 20

    cur_a.execute("""
        UPDATE inventario
        SET stock = stock - %s
        WHERE producto='Paracetamol'
    """, (cantidad,))

    print("\nStock descontado en Arequipa")

    raise Exception("Nodo Lima fuera de servicio")

except Exception as e:

    if conn_arequipa:
        conn_arequipa.rollback()

    if conn_lima:
        conn_lima.rollback()

    print("\nERROR:", e)
    print("Rollback ejecutado correctamente")

    cur_a.execute("SELECT stock FROM inventario WHERE producto='Paracetamol'")
    stock_a = cur_a.fetchone()[0]

    cur_l.execute("SELECT stock FROM inventario WHERE producto='Paracetamol'")
    stock_l = cur_l.fetchone()[0]

    print("\n=== STOCK DESPUÉS DEL ROLLBACK ===")
    print(f"Arequipa: {stock_a}")
    print(f"Lima: {stock_l}")

finally:

    if conn_arequipa:
        conn_arequipa.close()

    if conn_lima:
        conn_lima.close()