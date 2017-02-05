package org.protocolbench.enoceanspy;

import java.text.SimpleDateFormat;
import java.util.*;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList; 


public class EnOceanSpy {

    private static SerialPort serialPort;

	// default serial port name:
	static String serialPortName = "COM3";
	
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	System.out.println(getFormatedDate()+"Starting EnOcean Listener...\n");
   
    	// check command line argument
    	if (args.length >=2 || args.length == 0) {
			System.out.println("Usage: EnOceanSpy <comport>");
			System.out.println("Example: EnOceanSpy COM3");
			System.exit(1);
		} else {
			serialPortName = args[0];
		}
    	
    	String[] portNames = SerialPortList.getPortNames();
        
        if (portNames.length == 0) {
            System.out.println("There are no active serial ports available!");
            System.exit(1);
        }
        
		System.out.println("List of available ports: ");
        for (int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
        
        
        serialPort = new SerialPort(serialPortName);
        
        try {
            // opening port
            serialPort.openPort();
            
            serialPort.setParams(SerialPort.BAUDRATE_57600,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
            
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
                       
            System.out.println("Port " + serialPortName + " opened successfully, waiting for EnOcean telegrams...");
        }
        catch (SerialPortException ex) {
            System.out.println("Error while opening port: " + ex);
        }
    }
    
    // receiving telegram from COM port
    private static class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    //
                    String receivedData = serialPort.readHexString(event.getEventValue());
                    System.out.println(getFormatedDate()+ " > " + receivedData);
                }
                catch (SerialPortException ex) {
                    System.out.println("Error while receiving telegram: " + ex);
                }
            }
        }
    }
	
    // Generate time in usable format to interpret this information later
	private static String getFormatedDate(){
		SimpleDateFormat formatter = new SimpleDateFormat(
				"HH:mm:ss,SSS ");
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
}
