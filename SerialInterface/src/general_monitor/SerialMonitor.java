package general_monitor;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;

public class SerialMonitor implements SerialPortEventListener {

	SerialPort serialPort;
	private BufferedReader input;
	private BufferedWriter output;

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int BAUD = 9600;

	public void initialize() {

		CommPortIdentifier portId = null;
		@SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// find serial port matching COMX, X = 1,2,...,9
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (int i = 1; i < 10; i++) {
				if (currPortId.getName().equals("COM" + Integer.toString(i))) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it. Override
	 * this.
	 */
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			String inputLine = readln();
			if (inputLine != null)
				System.out.println("Received: " + inputLine);
		}

		// Ignore all the other eventTypes, but you should consider the other
		// ones.
	}

	/**
	 * Sends int-encoded data through the serial port.
	 * 
	 * @param code
	 */
	public synchronized void sendData(int code) {
		write(code);
	}

	public synchronized void write(int code) {
		try {
			output.write(Integer.toString(code));
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean serialReady() {
		try {
			return input.ready();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public synchronized String readln() {
		try {
			return input.ready() ? input.readLine() : null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
}
