package processor

import kotlin.math.pow
import kotlin.system.exitProcess

fun main() {
    while (true) {
        println("""
            1. Add matrices
            2. Multiply matrix by a constant
            3. Multiply matrices
            4. Transpose matrix
            5. Calculate a determinant
            6. Inverse matrix
            0. Exit
            """.trimIndent()
        )
        print("Your choice: ")
        when (readln().toIntOrNull()) {
            0 -> exitProcess(0)
            1 -> Processor().addMatrices()
            2 -> Processor().multiplyMatrixByConstant()
            3 -> Processor().multiplyMatrices()
            4 -> Processor().transposeMatrix()?.printMatrix()
            5 -> println(Processor().determinant())
            6 -> Processor().inverseMatrix()
            else -> println("Invalid choice. Please enter a number between 0 and 6.")
        }
    }
}

class Matrix {
    private var rows: Int = 0
    private var columns: Int = 0
    lateinit var matrix: Array<DoubleArray>

    fun dimensionsReader(numberOfMatrix: String) {
        val prompt = when (numberOfMatrix) {
            "one" -> "Enter size of matrix: "
            "oneFromTranspose" -> "Enter matrix size:"
            else -> "Enter size of $numberOfMatrix matrix: "
        }
        val (inputRows, inputColumns) = readDimensions(prompt)
        rows = inputRows
        columns = inputColumns
        matrixReader()
    }

    private fun readDimensions(prompt: String): Pair<Int, Int> {
        print(prompt)
        return readln().split(" ").map { it.toInt() }.let { (rows, columns) -> rows to columns }
    }

    private fun matrixReader() {
        println("Enter matrix:")
        matrix = Array(rows) {
            readRowValues()
        }
    }

    private fun readRowValues(): DoubleArray {
        return readln().split(" ").map { it.toDoubleOrNull() ?: 0.0 }.toDoubleArray()
    }

    fun printMatrix() {
        println("The result is:")
        for (row in 0 until rows) {
            println(matrix[row].joinToString(" "))
        }
        println()
    }

    private fun matrixLoader(matrix: Array<DoubleArray>) {
        rows = matrix.size
        columns = matrix[0].size
        this.matrix = matrix
    }

    operator fun plus(matrix2: Matrix): Matrix? {
        return if (rows == matrix2.rows && columns == matrix2.columns) {
            val product = Array(rows) { row ->
                DoubleArray(columns) { column ->
                    matrix[row][column] + matrix2.matrix[row][column]
                }
            }
            Matrix().apply { matrixLoader(product) }
        } else {
            println("The operation cannot be performed.")
            null
        }
    }

    operator fun times(constant: Double): Matrix {
        val product = Array(rows) { row ->
            DoubleArray(columns) { column ->
                matrix[row][column] * constant
            }
        }
        return Matrix().apply { matrixLoader(product) }
    }

    operator fun times(matrix2: Matrix): Matrix? {
        if (columns != matrix2.rows) {
            println("The operation cannot be performed.")
            return null
        }

        val product = Array(rows) { row ->
            DoubleArray(matrix2.columns) { column ->
                var sum = 0.0
                for (i in 0 until columns) {
                    sum += matrix[row][i] * matrix2.matrix[i][column]
                }
                sum
            }
        }
        return Matrix().apply { matrixLoader(product) }
    }

    fun transposeMainDiagonal(): Matrix {
        val transposedMatrix = Array(columns) { row ->
            DoubleArray(rows) { column ->
                matrix[column][row]
            }
        }
        return Matrix().apply { matrixLoader(transposedMatrix) }
    }

    fun transposeSideDiagonal(): Matrix {
        val transposedMatrix = Array(columns) { row ->
            DoubleArray(rows) { column ->
                matrix[rows - column - 1][columns - row - 1]
            }
        }
        return Matrix().apply { matrixLoader(transposedMatrix) }
    }

    fun transposeVerticalLine(): Matrix {
        val transposedMatrix = Array(rows) { row ->
            DoubleArray(columns) { column ->
                matrix[row][columns - column - 1]
            }
        }
        return Matrix().apply { matrixLoader(transposedMatrix) }
    }

    fun transposeHorizontalLine(): Matrix {
        val transposedMatrix = Array(rows) { row ->
            DoubleArray(columns) { column ->
                matrix[rows - row - 1][column]
            }
        }
        return Matrix().apply { matrixLoader(transposedMatrix) }
    }

    fun calculateDeterminant(): Double {
        if (rows != columns) {
            throw IllegalArgumentException("Determinant can only be calculated for square matrices.")
        }
        return calculateDeterminantRecursive(matrix)
    }

    private fun calculateDeterminantRecursive(matrix: Array<DoubleArray>): Double {
        val size = matrix.size
        return when (size) {
            1 -> matrix[0][0]
            2 -> matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]
            else -> {
                var determinant = 0.0
                for (i in 0 until size) {
                    val subMatrix = Array(size - 1) { DoubleArray(size - 1) }
                    for (j in 1 until size) {
                        var subMatrixColumn = 0
                        for (k in 0 until size) {
                            if (k != i) {
                                subMatrix[j - 1][subMatrixColumn++] = matrix[j][k]
                            }
                        }
                    }
                    determinant += matrix[0][i] * calculateDeterminantRecursive(subMatrix) * (-1.0).pow(i.toDouble())
                }
                determinant
            }
        }
    }

    fun calculateInverse(determinant: Double): Matrix {
        if (determinant == 0.0) {
            throw IllegalArgumentException("Matrix is not invertible.")
        }

        val cofactorMatrix = Array(rows) { row ->
            DoubleArray(columns) { column ->
                val subMatrix = Array(rows - 1) { DoubleArray(columns - 1) }
                var subMatrixRow = 0
                for (i in 0 until rows) {
                    if (i != row) {
                        var subMatrixColumn = 0
                        for (j in 0 until columns) {
                            if (j != column) {
                                subMatrix[subMatrixRow][subMatrixColumn++] = matrix[i][j]
                            }
                        }
                        subMatrixRow++
                    }
                }
                val cofactor = calculateDeterminantRecursive(subMatrix) * (-1.0).pow(row + column.toDouble())
                cofactor / determinant
            }
        }
        return Matrix().apply { matrixLoader(cofactorMatrix) }.transposeMainDiagonal()
    }

}

class Processor {
    private var matrix1: Matrix = Matrix()
    private var matrix2: Matrix = Matrix()

    fun addMatrices() {
        matrix1.dimensionsReader("first")
        matrix2.dimensionsReader("second")
        val productMatrix = matrix1.plus(matrix2)
        productMatrix?.printMatrix()
    }

    fun multiplyMatrixByConstant() {
        matrix1.dimensionsReader("one")
        val constant = readln().toDouble()
        val productMatrix = matrix1.times(constant)
        productMatrix.printMatrix()
    }

    fun multiplyMatrices() {
        matrix1.dimensionsReader("first")
        matrix2.dimensionsReader("second")
        val productMatrix = matrix1.times(matrix2)
        productMatrix?.printMatrix()
    }

    fun transposeMatrix(): Matrix? {
        println("""
            1. Main diagonal
            2. Side diagonal
            3. Vertical line
            4. Horizontal line
            """.trimIndent()
        )
        return when (readln().toInt()) {
            1 -> {
                matrix1.dimensionsReader("oneFromTranspose")
                matrix1.transposeMainDiagonal()
            }
            2 -> {
                matrix1.dimensionsReader("oneFromTranspose")
                matrix1.transposeSideDiagonal()
            }
            3 -> {
                matrix1.dimensionsReader("oneFromTranspose")
                matrix1.transposeVerticalLine()
            }
            4 -> {
                matrix1.dimensionsReader("oneFromTranspose")
                matrix1.transposeHorizontalLine()
            }
            else -> null
        }
    }

    fun determinant(): Double {
        matrix1.dimensionsReader("oneFromTranspose")
        println("The result is:")
        return matrix1.calculateDeterminant()
    }

    fun inverseMatrix() {
        matrix1.dimensionsReader("one")
        val determinant = matrix1.calculateDeterminant()
        if (determinant == 0.0) {
            println("This matrix doesn't have an inverse.")
        } else {
            val inverse = matrix1.calculateInverse(determinant)
            inverse.printMatrix()
        }
    }

}