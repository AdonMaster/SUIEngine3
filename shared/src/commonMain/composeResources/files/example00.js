@declare(
    parent_form = {}
    sub_form = {}
)

@form() {
    col() {
        textfield("company")

        @form(
            validation = {
                nome: [required(), min(5)],
                email: [required(), email()]
            },
            on_submit = @set($sub_form, @fields())
        ) {
            col(fill = 1, v_arrange = "center", h_align = "center") {
                textfield("nome")
                textfield("email")
                btn("submit", on_touch = @submit())
                btn("reset", on_touch = @reset())
            }
        }

        text($parent_form)
        text($sub_form)

        btn("submit", on_touch = @submit())
        btn("reset", on_touch = @reset())
    }
}



/*
@declare(base_button = btn($label, on_touch = $on_touch))
@declare(my_button = $base_button)

@state(btn1 = surface(
    background = isnull($background, "442233"),
    foreground = "ffffff",
    padding = [16, 0],
    elevation = 12.0,
    on_touch = $on_touch
) {
    row(
        padding = [20, 10],
    ) {
        text($label, font_weight = "bold")
    }
})

@state(
    cl_success = "008800",
    cl_info = "000088",
    cl_danger = "880000",
)

col(fill = 1, v_arrange = "center", h_align = "center") {
    @state(
        counter = 1,
    )
    @render(
        $btn1,
        label = $counter,
        background = $cl_success,
        on_touch = @set(counter = sum($counter, 1))
    )
}

//tested
/*@state(custom_triggers = [
    @set(counter = sum($counter, 1))
])
box(fill = 1, content_align = "center") {
    @state(counter = 1)
    btn(
        $counter,
        on_touch = $custom_triggers
    )
}*/


/*
@template("my_button", btn(vars = {
    label: "default label"
}) {
    text($label)
    @on("touch") {
        @slot("on_touch")
    }
})

box(fill = 1, content_align = "center") {
    @state(counter = 1)
    my_button(vars: {label: concat("Current: ", $counter)}) {
        @slot("on_touch") {
            @set(counter = sum($counter, 1))
        }
    }
}


/*
col(fill = 1, v_arrange = "center", padding = 20) {

    // 1. Dados dos Painéis (Objetos dentro de Array)
    @state(panels = [
        {
            bg: "0F172A",
            accent: "38BDF8",
            title: "🚀 Engine SDUI Nativa",
            body: "Arquitetura reativa de alta performance baseada em AST para Jetpack Compose."
        },
        {
            bg: "F8FAFC",
            accent: "0284C7",
            title: "⚡ Performance",
            body: "Renderização eficiente com virtualização de escopo e suporte a 50k+ itens."
        },
        {
            bg: "4C1D95",
            accent: "F472B6",
            title: "🎨 Design System Dinâmico",
            body: "Acesso em profundidade a propriedades e resoluções dinâmicas de índice em tempo real."
        },
        {
            bg: "064E3B",
            accent: "34D399",
            title: "🔒 Estabilidade e Resilience",
            body: "Tratamento de exceções e isolamento de runtime contra crashes de layout."
        }
    ])

    // 2. Controle de Estado e Variáveis Derivadas
    @state(currentIndex = 0)
    @state(totalPanels = array_len($panels))
    @state(activeIdx = mod($currentIndex, $totalPanels))
    @state(currentHero = $panels[$activeIdx])

    // 3. Card do Hero Panel
    box(
        w_fill = 1,
        background = $currentHero.bg,
        padding = 32,
        corner_radius = 16,
        content_align = "center"
    ) {
        col(w_fill = 1, v_arrange = "spaced", spacing = 12) {

            // Tag / Categoria com a cor de destaque do painel
            text(
                sum($activeIdx, 1), // Mostra o número do slider Ex: "1 / 4"
                color = $currentHero.accent,
                font_weight = "bold",
                size = 14
            )

            // Título Principal (cor chave dependendo do fundo)
            text(
                $currentHero.title,
                color = if_else(is_dark($currentHero.bg), "FFFFFF", "0F172A"),
                size = 22,
                font_weight = "bold"
            )

            // Corpo do texto
            text(
                $currentHero.body,
                color = if_else(is_dark($currentHero.bg), "CBD5E1", "475569"),
                size = 14
            )
        }
    }

    // 4. Bar de Navegação e Controles
    row(
        w_fill = 1,
        padding_top = 20,
        h_arrange = "space_between",
        v_align = "center"
    ) {
        surface(clickable = true) {
            text("Anterior")
            @on("touch") {
                @set(currentIndex = sum($currentIndex, -1))
            }
        }

        // Indicador simples de posição (Ex: "Painel 1 de 4")
        text(
            $activeIdx,
            color = "64748B",
            size = 12
        )

        btn("Próximo") {
            @on("touch") {
                @set(currentIndex = sum($currentIndex, 1))
            }
        }
    }
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

