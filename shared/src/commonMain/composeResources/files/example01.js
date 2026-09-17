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

