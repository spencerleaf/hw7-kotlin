sealed class MathExpressions{
    data class Num(val value: Int) : MathExpressions()
    data class Variable(val name: String) : MathExpressions()
    data class Negative(val name: MathExpressions) : MathExpressions()
    data class Add(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Sub(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Mul(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Div(val left: MathExpressions, val right: MathExpressions) : MathExpressions()
    data class Pow(val base: MathExpressions, val exp: MathExpressions) : MathExpressions()

}

fun main(){
    val x = MathExpressions.Num(5)
    println(x)
}