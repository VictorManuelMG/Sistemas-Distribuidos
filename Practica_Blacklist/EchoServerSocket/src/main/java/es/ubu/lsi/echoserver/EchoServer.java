package es.ubu.lsi.echoserver;

/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.net.*;
import java.io.*;

public class EchoServer {
	private static final int[] PUERTOS_BLOQUEADOS;
	
	//Generacion de un array de puertos bloqueados por un rango
    static {
        int startPort = 5000;
        int endPort = 90000;
        PUERTOS_BLOQUEADOS = new int[endPort - startPort + 1];
        for (int i = 0; i < PUERTOS_BLOQUEADOS.length; i++) {
            PUERTOS_BLOQUEADOS[i] = startPort + i;
        }
    }

	// Método para verificar si un puerto está bloqueado
	private static boolean puertoBloqueado(int puerto) {
		for (int puertoBloqueado : PUERTOS_BLOQUEADOS) {
			if (puerto == puertoBloqueado) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Usage: java EchoServer <port number>");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
		System.out.println("Escuchando por puerto: " + portNumber);

		try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Nuevo Cliente: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());
				// Verificar si el puerto del cliente está bloqueado
				if (puertoBloqueado(clientSocket.getPort())) {
					System.out.println("Puerto bloqueado: " + clientSocket.getPort());
					clientSocket.close(); // Cerrar la conexión
				} else {
					// Si el puerto no está bloqueado, manejar la conexión en un hilo
					ThreadServerHandler hiloNuevoCliente = new ThreadServerHandler(clientSocket);
					hiloNuevoCliente.start();
				}
			}

		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}

	}

}

class ThreadServerHandler extends Thread {

	private Socket clientSocket;

	public ThreadServerHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				System.out.println(clientSocket.getPort() + ":" + inputLine);
				out.println(inputLine);
			}
		} catch (IOException e) {
			System.out.println("Exception caught on thread");
			System.out.println(e.getMessage());
		}
	}
}