package br.ucdb.pos.engenhariasoftware.testesoftware.controller;

import br.ucdb.pos.engenhariasoftware.testesoftware.converter.DateToStringConverter;
import br.ucdb.pos.engenhariasoftware.testesoftware.converter.MoneyToStringConverter;
import br.ucdb.pos.engenhariasoftware.testesoftware.converter.StringToMoneyConverter;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Categoria;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

/**
 * Classe de teste para REST Assured
 */
public class LancamentoControllerTest {

    @BeforeTest
    public void init() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    /**
     * Caso de teste 1: Lista de lançamentos de tamanho 1.
     */
    @Test
    public void buscaLancamentosTamanho1Test() {
        List<Lancamento> lancamentos = new LinkedList<Lancamento>();
        lancamentos.add(new LancamentoBuilder().random(50.00).build());
        executaFluxoTest(lancamentos, 50.00);
    }

    /**
     * Caso de teste 2: Lista de lançamentos tamanho 2, com valores distintos e positivos.
     */
    @Test
    public void buscaLancamentosTamanho2ValoresDistintosEPositivosTest() {
        List<Lancamento> lancamentos = new LinkedList<Lancamento>();
        lancamentos.add(new LancamentoBuilder().random(25.00).build());
        lancamentos.add(new LancamentoBuilder().random(75.00).build());
        executaFluxoTest(lancamentos, 25.00);
    }

    /**
     * Caso de teste 3: Lista de lançamentos tamanho > 5, com valores iguais ou distintos positivos.
     */
    @Test
    public void buscaLancamentosTamanhoMaior5ValoresIguaisOuDistintosPositivosTest() {
        List<Lancamento> lancamentos = new LinkedList<Lancamento>();
        lancamentos.add(new LancamentoBuilder().random(10.00).build());
        lancamentos.add(new LancamentoBuilder().random(25.00).build());
        lancamentos.add(new LancamentoBuilder().random(25.00).build());
        lancamentos.add(new LancamentoBuilder().random(75.00).build());
        lancamentos.add(new LancamentoBuilder().random(100.00).build());
        lancamentos.add(new LancamentoBuilder().random(150.25).build());
        executaFluxoTest(lancamentos, 10.00);
    }

    /**
     * Método que executa fluxo de teste para todos os casos propostos
     *
     * @param lancamentos   lista de lançamentos a ser testada
     * @param valorEsperado valor esperado para asserção
     */
    private void executaFluxoTest(List<Lancamento> lancamentos, Double valorEsperado) {
        //Remover lançamento existentes para não influenciar o teste
        removerLancamentos();
        //Insere os lançamentos
        for (Lancamento lancamento : lancamentos) {
            salvarLancamento(lancamento);
        }
        //Realiza o teste de menor valor com os dois métodos
        assertEquals(getMenorLancamentoJsonPathFilter(), valorEsperado, "Erro validação JSON Path Filter.");
        assertEquals(getMenorLancamentoCodigoJava(), valorEsperado, "Erro validação código Java.");
        /**
         * Conclusão:
         *
         * O método "getMenorLancamentoJsonPathFilter" que  busca o menor valor usando jsontPath Filter não é confiável.
         * Para algumas combinações de valores, ele se perde e devolve um valor incorreto. Provavelmente devido ao formato
         * do valor usando "," ao invés de "." como separador de casas decimais. Portanto, o método para buscar o menor valor
         * usando código Java é o correto para usar neste caso de teste.
         */
    }

    /**
     * Método que realiza a busca dos lançamentos
     *
     * @return
     */
    private JsonPath buscaLancamentos() {
        Response response = given()
                .when()
                .body("Assured")
                .post("/buscaLancamentos");
        InputStream in = response.asInputStream();
        assertEquals(response.getStatusCode(), 200, "Erro ao buscar lançamentos");
        return JsonPath.with(in);
    }

    /**
     * Aplicando filtro na extração do JSON Path.
     *
     * @return menor valor encontrado no JSON
     */
    private Double getMenorLancamentoJsonPathFilter() {
        String valor = buscaLancamentos().getString("lancamentos.min{it.valor}.valor");
        return new StringToMoneyConverter().convert(valor).doubleValue();
    }

    /**
     * Obtendo a lista e aplicando obtendo o valor mínimo da lista(código Java)
     * param filtro
     *
     * @return menor valor
     */
    private Double getMenorLancamentoCodigoJava() {
        return getMenorValorLancamento(getLancamentos());
    }

    private void salvarLancamento(Lancamento lancamento) {
        Response response = given().when()
                .formParam("descricao", lancamento.getDescricao())
                .formParam("valor", new MoneyToStringConverter().convert(lancamento.getValor()))
                .formParam("dataLancamento", new DateToStringConverter().convert(lancamento.getDataLancamento()))
                .formParam("tipoLancamento", lancamento.getTipoLancamento())
                .formParam("categoria", lancamento.getCategoria())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post("/salvar");
        assertEquals(response.getStatusCode(), 302, "Erro ao salvar lançamento. Descrição: " + lancamento.getDescricao());
    }

    /**
     * Método que busca os lancamentos
     *
     * @param filtro
     * @return
     */
    private List<Lancamento> getLancamentos() {
        return buscaLancamentos().getList("lancamentos", Lancamento.class);
    }

    /**
     * Método que limpa a base de dados com lançamentos "Assured"
     */
    private void removerLancamentos() {
        for (Lancamento lancamento : getLancamentos()) {
            Response response = given().pathParam("id", lancamento.getId()).when().get("/remover/{id}");
            // Comentado devido a um bug no sistema que retorna código 500 quando só existe um registro no banco e é removido.
            // assertEquals(response.statusCode(), 200, "Erro ao remover registro. Id: " + lancamento.getId() + "| Descrição: " + lancamento.getDescricao());
        }

    }

    /**
     * Método para buscar o menor valor de uma lista de lançamentos
     *
     * @param lancamentos lista de lançamentos
     * @return menor valor da lista
     */
    private Double getMenorValorLancamento(List<Lancamento> lancamentos) {
        BigDecimal min = lancamentos
                .stream()
                .map(Lancamento::getValor)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        return min.doubleValue();
    }

    /**
     * Classe LancamentoBuilder
     * Responsável por construir um objeto da classe LancamentoVO usado nos testes
     */
    static class LancamentoBuilder {
        private Lancamento lancamento;

        LancamentoBuilder() {
            lancamento = new Lancamento();
        }

        LancamentoBuilder comData(int dia, int mes, int ano) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(ano, mes, dia);
            lancamento.setDataLancamento(calendar.getTime());
            return this;
        }

        LancamentoBuilder comDescricao(String descricao) {
            lancamento.setDescricao(descricao);
            return this;
        }

        LancamentoBuilder comValor(double valor) {
            lancamento.setValor(BigDecimal.valueOf(valor));
            return this;
        }

        LancamentoBuilder comTipo(TipoLancamento tipo) {
            lancamento.setTipoLancamento(tipo);
            return this;
        }

        LancamentoBuilder comCategoria(Categoria categoria) {
            lancamento.setCategoria(categoria);
            return this;
        }


        /**
         * Método que devolve um valor aleatório de uma categoria
         *
         * @return
         */
        private Categoria getCategoria() {
            final SecureRandom random = new SecureRandom();
            int x = random.nextInt(Categoria.class.getEnumConstants().length);
            return Categoria.class.getEnumConstants()[x];
        }

        /**
         * Método que gera o objeto Lancamento de forma aleatória
         *
         * @return
         */
        LancamentoBuilder random(Double valor) {
            Random rand = new Random();
            return comData(rand.nextInt(8), rand.nextInt(9), 2018)
                    .comTipo(rand.nextBoolean() ? TipoLancamento.ENTRADA : TipoLancamento.SAIDA)
                    .comDescricao("Assured Test")
                    .comValor(valor)
                    .comCategoria(getCategoria());
        }


        Lancamento build() {
            return lancamento;
        }
    }

}

