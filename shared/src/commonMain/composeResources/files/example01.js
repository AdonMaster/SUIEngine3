declare(
    click_count = 0,
    is_even = $click_count.mod(2).eq(0),
    status_label = $is_even.pick("PAR", "ÍMPAR"),
    user_title = "Olá, ".concat(default([2, $user_name], "Dev Anônimo")),
    threshold = 5,
    sixes = 6.repeat(3)
)

col(
    h_align = "center"
) {
    text($user_title)

    btn(
        $click_count,
        on_touch = @set(click_count = $click_count.add(1))
    )

    text(6.repeat(3))

    if ($is_even) {
        text("even")
    }
    else_if ($click_count.gt($threshold)) {
        text("odd greater than ".concat($threshold))
    } else {
        text("just odd!")
    }
}