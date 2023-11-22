package com.example.ports;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;



import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Serialports implements SerialPortDataListener {
    private  SerialPort serialPort;
    private String output=null;
    private OutputStream outputStream;
    private int counterByte;

    private final List<String> list=new ArrayList<>();
    private boolean fcs;

    private final SerialPort[] serialPorts = SerialPort.getCommPorts();

//#g-flag
    //$-esc
    //&-change flag
    //!change esc
    public  Serialports() {
        for (SerialPort port : serialPorts) {
            port.openPort();
            if (port.isOpen()) {
                serialPort=port;
                break;
            }
        }
        if(serialPort==null){
            System.exit(0);
        }
        open();
    }
    public String sendStringToComm(String command) throws IOException {
        byte[] data = command.getBytes();
        int crcValue = calculateCRC8(data);
        System.out.println(crcValue);
        System.out.printf("CRC-8: 0x%02X\n", crcValue);
        command=command.replace("$","$!");
        command=command.replace("#g","$&");
        byte b=(byte)Integer.parseInt(serialPort.getSystemPortName().replace("COM",""));
        StringBuilder str=new StringBuilder(String.format("%s%s%s","#g0","1", command));
        byte[] bt ={(byte)(crcValue)};
        str.append(new String(bt,"windows-1251"));
        byte[] bytes=str.toString().getBytes("windows-1251");
        outputStream.write(bytes);
        str.insert(str.length()-1," ");
        str.replace(3, 3, String.format("<html><font color='blue'>%x</font><html>", b));
        System.out.println(str);
        return str.toString();
    }

    @Override
    public int getListeningEvents() {
        return  SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }
    @Override
    public void serialEvent(SerialPortEvent event) {
        byte[] input = new byte[serialPort.bytesAvailable()];
        counterByte=serialPort.bytesAvailable();
        serialPort.readBytes(input,serialPort.bytesAvailable());
        String data;
        try {
            data = new String(input, "windows-1251");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String fcs=data.substring(data.length()-1);
        data=data.substring(4,data.length()-1);
       data=data.replace("$&","#g");
        data=data.replace("$!","$");
        try {
            errorBit(data,fcs.getBytes("windows-1251"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
                list.add(port.getSystemPortName().replaceAll("COM", ""));
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
    private static int calculateCRC8(byte[] data) {
        int crc = 0;

        for (byte b : data) {
            crc ^= b & 0xFF; // XOR текущего байта с текущим значением CRC

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x20) != 0) {
                    crc = ((crc << 1) ^ 0x2f); // Сдвиг влево и XOR с полиномом (0x3F)
                } else {
                    crc  <<= 1; // Просто сдвиг влево
                }
            }
        }

        return crc & 0x3F; // Обрезаем до 6 бит

    }

    private void errorBit(String data, byte[] fcs) throws UnsupportedEncodingException {
        Random random = new Random(System.currentTimeMillis());
        int wordLength = data.length();
        //System.out.printf("%d\n",fcs[0]);
        // Генерируем случайное число от 0 до 99
            int randomValue = random.nextInt(100);
            byte[] wordArray = data.getBytes("windows-1251");

            if (randomValue <= 30) {
                // Выбираем случайную позицию для искажения
                int errorPosition = random.nextInt(wordLength);

                // Инвертируем бит на этой позиции

                int errorbit = random.nextInt(8);
                byte mask = (byte) (1 << errorbit);
                wordArray[errorPosition] = (byte) (wordArray[errorPosition] ^ mask);

            }
            output=data;
        setFcs(calculateCRC8(wordArray) == (fcs[0]));
        System.out.println(calculateCRC8(wordArray));
        System.out.println(fcs[0] & 0x3F);
        //crc8ErrorFinder(fcs[0]& 0x3F,wordArray);
            System.out.println("Искаженное слово: " + new String(wordArray, "windows-1251"));
    }

    public void setFcs(boolean fcs){
        this.fcs=fcs;
    }
    public static void crc8ErrorFinder(int controlSum,byte[] data) throws UnsupportedEncodingException {
        byte[] data1 = data;
        System.out.println(controlSum);
        for (int dataIndex = 0; dataIndex < data1.length; dataIndex++) {
            for (int i = 7; i >= 0; i--) {
                data1[dataIndex] = (byte) (data1[dataIndex] ^ (1 << i));
                if(calculateCRC8(data1)==controlSum){
                    System.out.println(new String(data1,"windows-1251"));
                    //return new String(data,"windows-1251");
                }
                else {
                    data1[dataIndex] = (byte) (data1[dataIndex] ^ (1 << i));
                }
            }
        }
        //return null;
    }
    public boolean getFcs(){
        return fcs;
    }
}
