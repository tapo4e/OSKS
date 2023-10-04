package com.example.ports;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Serialports implements SerialPortDataListener {
    private  SerialPort serialPort;
    private String output=null;
    private OutputStream outputStream;
    private int counterByte;

    private final List<String> list=new ArrayList<>();

    private final SerialPort[] serialPorts = SerialPort.getCommPorts();


    public  Serialports() {
        for (SerialPort port : serialPorts) {
            port.openPort();
            if (port.isOpen()) {
                serialPort=port;
                System.out.println(serialPort.getSystemPortName());
                break;
            }
        }
        if(serialPort==null){
            System.exit(0);
        }
        open();
    }
    public void sendStringToComm(String command) throws IOException {
        outputStream.write(command.getBytes((StandardCharsets.UTF_8)));
    }

    @Override
    public int getListeningEvents() {
        return  SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }


    @Override
    public void serialEvent(SerialPortEvent event) {
        byte[] input = new byte[serialPort.bytesAvailable()];
        counterByte+=serialPort.bytesAvailable();
        serialPort.readBytes(input,serialPort.bytesAvailable());
        output = new String(input, StandardCharsets.UTF_8);
    }



    public String getOutput() {
        return output;
    }

    public String getCounterByte(){
        return String.valueOf(counterByte);
    }
    public SerialPort getSerialPort() {
        return serialPort;
    }



    public void setOutput() {
        output = null;
    }

    public String getBaudRate(){
        return  String.valueOf(serialPort.getBaudRate());
    }

    public List<String> getList(){

        for(SerialPort port : serialPorts){
            list.add(port.getSystemPortName().replaceAll("COM",""));
        }
        return list;
    }
    public  Boolean OpenPort(String name){
        SerialPort port = SerialPort.getCommPort(name);
        port.openPort();
        if(!port.isOpen()){
            System.out.println("false1");
            return false;
        }
        else {
            serialPort.closePort();
            serialPort=port;
            open();
            return  true;
        }
    }
    private void open(){
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setNumDataBits(8);
        serialPort.addDataListener(this);
        serialPort.setBaudRate(9600);
        outputStream=serialPort.getOutputStream();
        counterByte = 0;
    }

}
