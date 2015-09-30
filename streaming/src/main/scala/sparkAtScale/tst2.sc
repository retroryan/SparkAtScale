import scala.annotation.tailrec
def atoi(chList:List[String]):Int = {

  @tailrec
  def atoiAccumulator(chList: List[String], accumulator: Int): Int = chList match {
    case Nil => accumulator

    case head :: tail =>
      val tensMult = scala.math.pow(10, tail.length).toInt
      val nxtAccumulator = (head.toInt * tensMult) + accumulator
      atoiAccumulator(tail, nxtAccumulator)
  }

  atoiAccumulator(chList, 0)
}

val tst1 = atoi(List("1","3","5", "7","2","9"))
assert(tst1 == 135729, "Error in atoi")
val tst2 = atoi(List("4","2","8", "7","1","9"))
assert(tst2 == 428719, "Error in atoi")
val tst3 = atoi(List("0","2","8", "7","1","9"))
assert(tst3 == 28719, "Error in atoi")


val sc = try {
  val t = 3
  t*42
}

println(s"$sc")