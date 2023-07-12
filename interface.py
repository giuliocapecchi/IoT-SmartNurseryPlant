import tkinter as tk
import subprocess

def start_border_router():
    # Esegue il comando 'docker start -ai 59ecd26cffb2' tramite il modulo subprocess
    subprocess.run(['docker', 'start', '-ai', '59ecd26cffb2'])

    # Esegue i comandi all'interno del container Docker
    subprocess.run(['docker', 'exec', '59ecd26cffb2', 'bash', '-c', 'cd IoTProject/rpl-border-router/ && make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 connect-router'])

# Crea la finestra principale
window = tk.Tk()

# Crea il pulsante "Start Border Router"
button = tk.Button(window, text="Start Border Router", command=start_border_router)
button.pack()

# Avvia il ciclo dell'interfaccia grafica
window.mainloop()
