package package1.package2;

public class JavaInSubPackage {
  public static void main(String... args) {
    final JavaInSubPackage someJava = new JavaInSubPackage();
    System.out.println(someJava.hello());
  }
  
  public String hello() {
    return "hello world!";
  }
}
