box(
    align = "center", padding = 32, fill = 1, background = "#F8FAFC"
) {
    col(align = "center") {
        @state(email = "")
        @state(password = "")
        @state(isPasswordVisible = false)
        @state(rememberMe = false)
        @state(isLoading = false)

        // Cabeçalho da Tela
        box(align = "center", padding = 16) {
            col(align = "center") {
                // Simulação de ícone / logo com bordas arredondadas e sombra
                box(
                    width = 72, height = 72,
                    shape = "circle",
                    background = "#E0E7FF",
                    align = "center"
                ) {
                    text("🔒", size = 28)
                }

                spacer(size = 16)

                text("Acesse sua conta", size = 24, weight = "bold", color = "#0F172A")
                spacer(size = 6)
                text("Entre com suas credenciais para continuar", size = 14, color = "#64748B")
            }
        }

        spacer(size = 24)

        // Seção de Inputs com Modificadores de Estilo
        col(gap = 12) {
            input(
                placeholder = "Seu e-mail profissional",
                value = $email,
                background = "#FFFFFF",
                borderColor = "#E2E8F0",
                borderWidth = 1,
                radius = 12,
                padding = 16,
                elevation = 2
            ) {
                on("change") {
                    @set($email, $value)
                }
            }

            box(position = "relative") {
                input(
                    placeholder = "Sua senha segura",
                    value = $password,
                    secure = @not($isPasswordVisible),
                    background = "#FFFFFF",
                    borderColor = "#E2E8F0",
                    borderWidth = 1,
                    radius = 12,
                    padding = 16,
                    elevation = 2
                ) {
                    on("change") {
                        @set($password, $value)
                    }
                }

                // Botão de mostrar/esconder senha posicionado internamente
                box(align = "end-center", padding = 16) {
                    btn() {
                        text(@if($isPasswordVisible, "👁️", "🙈"), size = 16)
                        on("touch") {
                            @set($isPasswordVisible, @not($isPasswordVisible))
                        }
                    }
                }
            }
        }

        spacer(size = 16)

        // Linha auxiliar: "Lembrar de mim" e "Esqueceu a senha"
        row(justify = "space-between", align = "center") {
            row(align = "center", gap = 8) {
                checkbox(checked = $rememberMe, radius = 6) {
                    on("change") {
                        @set($rememberMe, $value)
                    }
                }
                text("Lembrar de mim", size = 13, color = "#475569")
            }

            btn() {
                text("Esqueceu a senha?", size = 13, color = "#2563EB", weight = "medium")
                on("touch") {
                    // Ação de recuperar senha
                }
            }
        }

        spacer(size = 32)

        // Botão de Ação Principal (CTA) com Estado de Loading
        btn(
            background = "#2563EB",
            radius = 12,
            padding = 16,
            elevation = 4,
            disabled = @or(@empty($email), @empty($password))
        ) {
            box(align = "center") {
                @if($isLoading) {
                    row(align = "center", gap = 8) {
                        spinner(size = 20, color = "#FFFFFF")
                        text("Entrando...", color = "#FFFFFF", weight = "bold", size = 16)
                    }
                } @else {
                    text("Entrar na Plataforma", color = "#FFFFFF", weight = "bold", size = 16)
                }
            }
            on("touch") {
                @set($isLoading, true)
                // Dispara o fluxo KMP / ViewModel
            }
        }

        spacer(size = 24)

        // Divisor "OU"
        row(align = "center", gap = 16) {
            divider(height = 1, color = "#E2E8F0", weight = "flex")
            text("ou continue com", size = 12, color = "#94A3B8")
            divider(height = 1, color = "#E2E8F0", weight = "flex")
        }

        spacer(size = 24)

        // Botões sociais secundários
        row(gap = 12) {
            btn(background = "#FFFFFF", borderColor = "#E2E8F0", borderWidth = 1, radius = 12, padding = 12, weight = "flex") {
                row(align = "center", justify = "center", gap = 8) {
                    text("🌐", size = 16)
                    text("Google", size = 14, color = "#1E293B", weight = "medium")
                }
                on("touch") {}
            }
            btn(background = "#FFFFFF", borderColor = "#E2E8F0", borderWidth = 1, radius = 12, padding = 12, weight = "flex") {
                row(align = "center", justify = "center", gap = 8) {
                    text("", size = 16)
                    text("Apple", size = 14, color = "#1E293B", weight = "medium")
                }
                on("touch") {}
            }
        }
    }
}