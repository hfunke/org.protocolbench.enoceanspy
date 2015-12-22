package org.protocolbench.enoceanspy;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EnOceanSpy {
	
	static SerialPort serialPort;
	
	static String serialPortName = "COM3";
		

	void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.err.println("Port is currently in use!");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					3000);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;

				// settings for EnOcean:
				serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				InputStream in = serialPort.getInputStream();
			
				serialPort.addEventListener(new SerialReader(in));
				serialPort.notifyOnDataAvailable(true);
				
			} else {
				System.err.println("Only serial ports are handled!");
			}
		}
	}

	public static class SerialReader implements SerialPortEventListener {
		private InputStream in;
		private byte[] buffer = new byte[1024];

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void serialEvent(SerialPortEvent arg0) {

			java.awt.Toolkit.getDefaultToolkit().beep();
			int data;
			try {
				int len = 0;
				while ((data = in.read()) > -1) {
					buffer[len++] = (byte) data;
				}
				if (len > 3) {
					byte[] incomingTelegram = new byte[len];
					System.arraycopy(buffer, 0, incomingTelegram, 0, len);
					
					System.out.print("\n"+ getFormatedDate() + "  > "
							+ byteArrayToHex(incomingTelegram) + "\n");
				
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}


	
	private static String getFormatedDate(){
		SimpleDateFormat formatter = new SimpleDateFormat(
				"HH:mm:ss,SSS ");
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
	
	

	private static String byteArrayToHex(byte[] a) {

		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			sb.append(String.format("%02x", b & 0xff));
			sb.append(" ");
		}

		return sb.toString().toUpperCase();
	}
	
	

	public static void main(String[] args) {

		System.out.println(getFormatedDate()+"Starting EnOcean Listener...\n");
		
		
		try {
			(new EnOceanSpy()).connect(serialPortName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}