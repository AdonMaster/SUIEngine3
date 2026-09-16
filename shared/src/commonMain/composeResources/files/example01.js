col(background = "c8c8c8", padding = 50) {

    @state(obj = {a: 1, b: "dois", c: {f: 4}})

    text($obj.a)

}

/*
col(fill = 1) {
    @state(colors = [
        "1e3c72", // Azul Noite
            "2a5298", // Azul Intermediário
            "366cb8", // Azul Céu Escuro
            "4785d8", // Azul Céu
            "5aa0f8", // Azul Claro
            "78b3ff", // Azul Pastel
            "9bc7ff", // Azul Gelo
            "bfe1ff"  // Branco Azulado
    ])
    @foreach($colors) {
        box(
            w_fill = 1, background = $it, padding = 20, weight = toFloat(sum($index, 1))
        )
    }
}


col(fill = 1) {

    @state(items = [
        {id: 1, user: "Mongoli"},
        {id: 2, user: "Atoa"},
        {id: 3, user: "gibanu"},
    ])

    lazy_col(items = $items, weight = 1.0) {

        text(
            w_fill = 1,
            concat("payload: ", $it),
            padding = 1,
            background = "c8c8c8",
            padding = 20
        )

    }

}
*/

