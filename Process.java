
public class Process extends Thread {
    private String name;
    private int pages;

    public Process(String name, int pages) {
        super(name);
        this.pages = pages;
        this.name = name;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public void run() {
        try {
            System.out.print(Thread.currentThread().toString());
            System.out.println(" is running in Thread: " + Thread.currentThread().getId());

        } catch (Exception e) {
            System.out.println("Exception is caught");
        }

    }

    @Override
    public String toString() {
        return "Process [name=" + name + ", pages=" + pages + "]";
    }

}