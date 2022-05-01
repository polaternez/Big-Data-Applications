package com.xbank.bigdata.efthavale.producer;

import netscape.javascript.JSObject;
import org.apache.kafka.common.protocol.types.Field;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.*;

public class DataGenerator{

    public static Random rand=new Random();
    public static List<String> names = new ArrayList<>();
    public static List<String> surnames = new ArrayList<>();

    public static int pid=10000;

    public DataGenerator() throws FileNotFoundException {
        File fileName = new File("C:\\Users\\Master\\Desktop\\isimler.txt");
        File fileSurname = new File("C:\\Users\\Master\\Desktop\\soyisimler.txt");

        Scanner fileNameScanner = new Scanner(fileName);
        Scanner fileSurnameScanner = new Scanner(fileSurname);

        while (fileNameScanner.hasNext()) {
            names.add(fileNameScanner.nextLine());
        }
        while (fileSurnameScanner.hasNext()) {
            surnames.add(fileSurnameScanner.nextLine());
        }
//        names.forEach(f-> System.out.println(f));
//        surnames.forEach(f-> System.out.println(f));
    }

    public String generate(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<String> btype = Arrays.asList("TL", "USD", "EUR");

        JSONObject data = new JSONObject();
        data.put("pid",pid++);
        data.put("ptype","H");

        JSONObject account = new JSONObject();
        account.put("oid",generateID());
        account.put("title",generateNameAndSurname());
        account.put("iban","TR"+generateID());

        data.put("account",account);

        JSONObject info = new JSONObject();
        info.put("title",generateNameAndSurname());
        info.put("iban","TR"+generateID());
        info.put("bank","X Bank");

        data.put("info",info);

        data.put("balance",rand.nextInt(999999));
        data.put("btype",btype.get(rand.nextInt(btype.size())));
        data.put("current_ts",timestamp);

        return data.toString();
    }

    public static long generateID(){

        long id=10000000000L+(long)(rand.nextDouble()*9999999999L);
        return id;
    }

    public static String generateNameAndSurname(){
        String name=names.get(rand.nextInt(names.size()));
        String surname=surnames.get(rand.nextInt(surnames.size()));

        return name+" "+surname;
    }
}
