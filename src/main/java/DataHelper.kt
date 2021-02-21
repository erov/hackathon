import com.github.nwillc.poink.PSheet
import com.github.nwillc.poink.workbook

object DataHelper {
    var restPrior: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var restReq: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var qualified: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var personalLevel: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var start: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var startRight: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var params: MutableMap<String, Int> = mutableMapOf()
    var maxFly: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var requiredPersonal: MutableList<MutableList<Int>> = MutableList(0) { MutableList(0) { 0 } }
    var maxPersonalLevel: Int = 0

    fun init(name: String) {
        workbook(name) {
            sheet("RestPrior ") {
                restPrior = parse()
            }
            sheet("RestReq ") {
                restReq = parse()
            }
            sheet("Qualified ") {
                val temp = parse(this.first().lastCellNum.toInt())
                for (i in 0 until temp[0].size) {
                    val tempQ = mutableListOf<Int>()
                    for (j in 0 until 10) {
                        tempQ.add(temp[j][i])
                    }
                    qualified.add(tempQ)
                }
            }
            sheet("PersonalLevel ") {
                personalLevel = parse(2)
                maxPersonalLevel = personalLevel.maxByOrNull { it[1] }!![1]
            }
            sheet("params ") {
                for (i in this.asIterable()) {
                    params[i.getCell(0).toString()] = i.getCell(1).numericCellValue.toInt()
                }
            }
            sheet("Starts ") {
                start = parse(1)
            }
            sheet("maxStarts ") {
                startRight = parse(1)
            }
            sheet("MaxFly ") {
                startRight = parse(1)
            }
            sheet("RequiredPersonal ") {
                requiredPersonal = parse()
            }
        }
    }

    fun getParam(param: String): Int = params[param]!!

    fun isQualified(id: Int, j: Int) = qualified[id][j] == 1

    private fun PSheet.parse(count: Int = 12): MutableList<MutableList<Int>> {
        val req = MutableList(0) { mutableListOf<Int>() }
        for (row in this.asIterable()) {
            val temp = MutableList(0) { 0 }
            for (i in 0 until count) {
                temp.add(row.getCell(i).numericCellValue.toInt())
            }
            req.add(temp)
        }
        return req
    }
}
