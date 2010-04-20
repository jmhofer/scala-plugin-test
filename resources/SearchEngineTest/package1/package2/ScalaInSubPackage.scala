package package1.package2

object ScalaInSubPackage {
  val hello1: Int => Unit = _ => { println("hello1") }
  
  def main(args: Array[String]) : Unit = {
    val something = new ScalaInDefaultPackage
    println("hello world")
    hello1(42)
    something.hello2(-1)
  }
}

class ScalaInDefaultPackage {
  val hello2: Int => Unit = _ => { println("hello2")}
}