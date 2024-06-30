import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;


public class Concurrency{

    public static Optional<String> getProductData(Path pathToData){
        ExecutorService threadService = null;
        try{
            threadService = Executors.newSingleThreadExecutor();
            Future<String> dataStream = threadService.submit(()->{
                String strData = readDataFile(pathToData);
                return strData;
            });
            return Optional.of(dataStream.get());
        }catch(Exception e){
            System.err.println("An exception occured..");
            e.printStackTrace();
            return Optional.empty();
        }
        finally{
            if(threadService != null)
                threadService.shutdown();
        }
        // if(threadService != null){
        //     threadService.awaitTermination(2, TimeUnit.SECONDS);
        // }
    }

    private static String readDataFile(Path pathToData){
        try(var data = Files.lines(pathToData)){
            StringBuilder sb = new StringBuilder();
            data.forEach(str->sb.append(str+"\n"));
            return sb.toString();
        }catch(IOException e){
            System.err.println("An IO / File error occured.. ");
            e.printStackTrace();
            return null;
        }
    }

    // Checking inventory every hours
    public static void periodicInventoryCheck(List<Product> inventory){
        ScheduledExecutorService threadService = null;
        try {
            threadService = Executors.newSingleThreadScheduledExecutor();
            threadService.scheduleWithFixedDelay(
                ()->{
                    try {
                        checkInventory(inventory);
                    } catch (UnavailableProductException e) {
                        e.printStackTrace();
                    }
                }, 1, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            System.err.println("An exception occured..");
            e.printStackTrace();
        }
    }

    public static void main(String... args){
        System.out.println(Concurrency.getProductData(Path.of("product.txt")).orElse("No data was returned"));
        //productData.forEach();
    }

    private static void checkInventory(List<Product> products) throws UnavailableProductException{
        for(Product p : products){
            if(p.getQuantity() == 0)
                throw  new UnavailableProductException(String.format("No  %s found in inventory!", p.getName()));
        }
        
    }
}

class Client implements Runnable{

    @Override
    public void run(){

    }
}

class Product{

    private String name;
    private final String ID;
    private double price;
    private AtomicLong quantity;
    private String size;
    private String color;
    private double weight;

    public Product(){
        ID = "00-000-000";
    }

    public Product(String name, String id, double price, long quantity, String size, 
        String color, double weight){
        this.name = name;
        this.ID = id;
        this.price = price;
        this.quantity = new AtomicLong(quantity);
        this.size = size;
        this.color = color;
        this.weight = weight;
    }

    public double getPrice(){
        return this.price;
    }
    public long getQuantity(){ return this.quantity.get();}
    public String getName(){return this.name;}
    public synchronized void setPrice(double price){
        this.price = price;
    }

    public synchronized void setWeight(double w){this.weight = w;}
    public synchronized void setColor(String c){this.color = c;}


}

class UnavailableProductException extends Exception{

    private String message;
    private UnavailableProductException exception;

    public UnavailableProductException(){
        super();
    }

    public UnavailableProductException(String msg){
        super(msg);
        this.message = msg;
    }

    public UnavailableProductException(UnavailableProductException e){
        super(e);
    }
}