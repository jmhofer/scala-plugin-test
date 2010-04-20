
public class JavaInDefaultPackage {
  public static void main(String... args) {
    final JavaInDefaultPackage someJava = new JavaInDefaultPackage();
    System.out.println(someJava.hello());
  }
  
  public String hello() {
    return "hello world!";
  }
}
