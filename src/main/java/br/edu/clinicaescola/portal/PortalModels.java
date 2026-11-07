package br.edu.clinicaescola.portal;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class PortalModels {
    private PortalModels() {}
    public record Pessoa(@NotBlank @Size(max=120) String nome,
                         @NotBlank @Email @Size(max=160) String email,
                         @NotBlank @Size(min=12,max=64) String senha,
                         @NotBlank String perfil) {}
    public record PessoaEdicao(@NotBlank @Size(max=120) String nome,@NotBlank String perfil,@NotNull Boolean ativo) {}
    public record Curso(@NotBlank @Size(max=30) String codigo,@NotBlank @Size(max=160) String nome,
                        @Min(1) @Max(20000) int cargaHoraria) {}
    public record Disciplina(@NotNull @Positive Long cursoId,@NotBlank @Size(max=30) String codigo,
                             @NotBlank @Size(max=160) String nome,@Min(1) @Max(20000) int cargaHoraria) {}
    public record Turma(@NotNull @Positive Long disciplinaId,@NotNull @Positive Long professorId,
                        @NotBlank @Size(max=160) String nome,@NotBlank @Size(max=30) String periodo,
                        @Min(1) @Max(1000) int vagas) {}
    public record Matricula(@NotNull @Positive Long turmaId,@NotNull @Positive Long alunoId) {}
    public record Aula(@NotNull @Positive Long turmaId,@NotBlank @Size(max=160) String titulo,
                       @NotBlank @Size(max=12000) String conteudo,@Size(max=2000) String materialUrl) {}
    public record Atividade(@NotNull @Positive Long turmaId,@NotBlank @Size(max=160) String titulo,
                            @NotBlank @Size(max=12000) String instrucoes,@NotNull Instant prazo) {}
    public record Entrega(@NotNull @Positive Long atividadeId,@NotBlank @Size(max=12000) String conteudo) {}
    public record Avaliacao(@NotNull @DecimalMin("0") @DecimalMax("10") @Digits(integer=2,fraction=2) BigDecimal nota,
                            @Size(max=4000) String feedback,@NotNull Boolean publicada) {}
    public record Lancamento(@NotNull @Positive Long alunoId,@NotBlank @Size(max=240) String descricao,
                             @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal valor,
                             @NotNull LocalDate vencimento) {}
}
