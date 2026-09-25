
declare(show = false)
btn(
    "show it",
    on_touch = set(show = $show.not())
)

if ($show) {
    text("bot_start", align = "bot_start", background = "red")
}

if ($show) {
    declare(leave = set(show = $show.not()))

    dialog(on_dismiss = $leave) {
        surface(bg = "black", fg = "white", elevation = 10) {
            col(padding = 20, h_align = "center", v_arrange = "spaced_by:20") {
                text("eu sou assim!!!")
                btn("Confirmar", on_touch = $leave)
            }
        }
    }
}

/*
surface(
    bg = "blue", fg = "white", corner_radius = 8, elevation = 4,
    padding = 20,
    on_touch = nop()
) {
    text(
        "adon",
        padding = [10, 20]
    )
}


/*
declare(
    styles = ["label", "body", "title", "head", "display"],
    sizes = ["_sm", "", "_lg"]
)
col(
    h_align = "start"
) {

    for_each($styles, as = "style") {
        for_each($sizes, as = "size") {
            declare(res = "${style}${size}")
            row(h_arrange = "spaced_by:8", v_align = "center") {
                text("${res}")
                text(">")
                text("Adon", style = $res)
            }
        }
    }

}

/*
col {
    box(background = "red", w_fill = 0.25, weight = 1)
    box(background = "yellow", w_fill = 0.50, weight = 1)
    box(background = "green", w_fill = 0.75, weight = 1)
    box(background = "gray", w_fill = 1, weight = 1)
}

/*
    declare(arr = [
            "top_start",
            "top_center",
            "top_end",
            "center_start",
            "center",
            "center_end",
            "bot_start",
            "bot_center",
            "bot_end"
        ]
    )
    for_each($arr) {
        text($index.add(1), align = $item, background = "yellow", padding = 20)
    }


/*
form(
    name = "macada", h_align = "center"
) {
    col() {
        input(name = "first_name", label = "First name")
        input(name = "last_name", label = "Last name")
    }

    declare(full_name = "${form.first_name} ${form.last_name}")

    text(
        "Nome completo: ${full_name}"
    )

    declare(
        counter = 1,
        arr = [1, 2, 3, 4]
    )
    btn(
        "${counter}",
        on_touch = set(counter = $counter.add($counter))
    )
}
