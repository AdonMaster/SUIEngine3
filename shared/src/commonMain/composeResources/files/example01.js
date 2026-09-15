@state(count = 1)
col() {
    text($count)
    btn("Increment") {
        @on("touch") {
            @set(count = sum($count, 1))
        }
    }
}