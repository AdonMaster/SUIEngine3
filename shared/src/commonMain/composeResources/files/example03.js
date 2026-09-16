// working so far
col(fill = true, vertical_arrangement = "space_between", padding = 0, background = "#0F172A") {

    @state(padding = 0)

    // Header do painel de controle com state binding
    row(fill_w = true, horizontal_arrangement = "space_between", vertical_alignment = "center", padding = [$padding, $padding, 12, 16], background = "#1E293B") {
        text("SUIEngine", style = "head_sm", color = "#F8FAFC", font_weight = "bold")
        text(concat("Padding: ", $padding, " px"), color = "#38BDF8", font_size = 14)
    }

    // Grid central com pesos misturados e caixas coloridas aninhadas
    col(fill_w = true, weight = 1.0, vertical_arrangement = "space_between") {

        // Linha superior: Grid 2 colunas com pesos iguais
        row(fill_w = true, weight = 1.0, horizontal_arrangement = "space_between") {
            box(weight = 1.0, fill_h = true, background = "#8B5CF6", content_align = "center", padding = 8) {
                text("Módulo Alfa", color = "#FFFFFF", font_weight = "bold")
            }

            spacer(w = 12)

            box(weight = 1.0, fill_h = true, background = "#EC4899", content_align = "center", padding = 8) {
                text("Módulo Beta", color = "#FFFFFF", font_weight = "bold")
            }
        }

        spacer(h = 12)

        // Linha inferior: Três blocos com proporções assimétricas (1.0, 2.0, 1.0)
        row(fill_w = true, weight = 2.0, horizontal_arrangement = "space_between") {
            box(weight = 1.0, fill_h = true, background = "#10B981", content_align = "bottom_start", padding = 12) {
                text("v1.2", color = "#064E3B", font_size = 12, font_weight = "bold")
            }

            spacer(w = 8)

            box(weight = 2.0, fill_h = true, background = "#6366F1", content_align = "center") {
                text("Core Engine Ativo", color = "#E0E7FF", font_weight = "bold")
            }

            spacer(w = 8)

            box(weight = 1.0, fill_h = true, background = "#F43F5E", content_align = "top_end", padding = 12) {
                text("Alert", color = "#FFE4E6", font_size = 12, font_weight = "bold")
            }
        }
    }

    // Rodapé interativo com botões de ajuste de estado
    row(fill_w = true, horizontal_arrangement = "space_around", padding = [8, 0, 8, 0]) {
        btn("Aumentar Padding") {
            @on("touch") {
                @set(padding = sum($padding, 4))
            }
        }
        btn("Reduzir Padding") {
            @on("touch") {
                @set(padding = sum($padding, -4))
            }
        }
    }
}