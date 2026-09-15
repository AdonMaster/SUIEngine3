@state(count = 1)
col() {
    spacer(weight = 1.0)
    row() {
        spacer(weight = 1.0)
        btn() {
            @on("touch") {
                @set(count = sum($count, sum(0, 1)))
            }
            text($count)
        }
        spacer(weight = 1.0)
    }
    spacer(weight = 1.0)
}
