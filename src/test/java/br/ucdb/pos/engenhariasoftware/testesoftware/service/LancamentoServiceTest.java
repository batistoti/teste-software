package br.ucdb.pos.engenhariasoftware.testesoftware.service;

import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.ResultadoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento.ENTRADA;
import static br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento.SAIDA;
import static br.ucdb.pos.engenhariasoftware.testesoftware.util.Constantes.DD_MM_YYYY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class LancamentoServiceTest {

    @Mock
    private LancamentoService lancamentoService;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void initMocks() {
        when(lancamentoService.getTotalEntrada(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalSaida(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.somaValoresPorTipo(anyListOf(Lancamento.class), any(TipoLancamento.class))).thenCallRealMethod();

    }

    @DataProvider(name = "lancamentos")
    protected Object[][] getLancamentos() {
        return new Object[][]{
                new Object[]{
                        Arrays.asList(
                                new LancamentoBuilder().comDescricao("Salário").comData("05/07/2018").comValor(5000.00).comTipo(ENTRADA).build(),
                                new LancamentoBuilder().comDescricao("Benefícios").comData("05/07/2018").comValor(170.00).comTipo(ENTRADA).build(),
                                new LancamentoBuilder().comDescricao("Academia").comData("05/07/2018").comValor(75.00).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Parcela Carro").comData("05/07/2018").comValor(1227.00).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Cartão Nubank").comData("06/07/2018").comValor(876.78).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Cartão Submarino").comData("06/07/2018").comValor(455.78).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Parcela Carro").comData("05/07/2018").comValor(455.78).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Pós-Graduação").comData("10/07/2018").comValor(296.88).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Abastecimento").comData("08/07/2018").comValor(120.00).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comDescricao("Refeição").comData("13/07/2018").comValor(94.00).comTipo(SAIDA).build()
                        ), "Salário", (long) 1
                }
        };
    }

    /**
     * Método de teste responsável por validar o total de entradas da lista de lançamentos
     *
     * @param lancamentos
     * @param itemBusca
     * @param tamanhoEsperado
     */
    @Test(dataProvider = "lancamentos")
    public void getTotalEntradaTest(List<Lancamento> lancamentos, String itemBusca, long tamanhoEsperado) {
        final BigDecimal totalEsperado = BigDecimal.valueOf(5170.00);
        final BigDecimal totalObtido = lancamentoService.getTotalEntrada(lancamentos);
        assertEquals(totalObtido, totalEsperado);

    }

    /**
     * Método de teste responsável por validar o total de entradas da lista de lançamentos
     *
     * @param lancamentos
     * @param itemBusca
     * @param tamanhoEsperado
     */
    @Test(dataProvider = "lancamentos")
    public void getTotalSaidaTest(List<Lancamento> lancamentos, String itemBusca, long tamanhoEsperado) {
        final BigDecimal totalEsperado = BigDecimal.valueOf(3601.22);
        final BigDecimal totalObtido = lancamentoService.getTotalSaida(lancamentos);
        assertEquals(totalObtido, totalEsperado);
    }

    /**
     * Teste responsável por validar o tamanho da lista de lançamentos
     *
     * @param lancamentos
     * @param itemBusca
     * @param tamanhoEsperado
     */
    @Test(dataProvider = "lancamentos")
    public void getTamanhoListaTest(List<Lancamento> lancamentos, String itemBusca, long tamanhoEsperado) {
        when(lancamentoService.busca(itemBusca)).thenReturn(lancamentos);
        when(lancamentoService.conta(itemBusca)).thenReturn((long) lancamentos.size());
        when(lancamentoService.getResultadoVO(lancamentos, this.tamanhoPagina, lancamentos.size())).thenCallRealMethod();
        given(lancamentoService.buscaAjax(itemBusca)).willCallRealMethod();
        long tamanhoObtido = lancamentoService.buscaAjax(itemBusca).getTotalRegistros();
        String mensagem = "Na busca por '" + itemBusca + "', era esperado que a pesquisa retornasse " + tamanhoEsperado + " registro(s), mas foi retornado " + tamanhoObtido + " registro(s).";
        assertEquals(tamanhoObtido, tamanhoEsperado, mensagem);
        verify(lancamentoService, atLeastOnce()).buscaAjax(itemBusca);
    }

    /**
     * Método de teste responsával por garantir que todo atributo da classe Lancamento esteja na LancamentoVO e não nula
     *
     * @param lancamentos
     * @param itemBusca
     * @param tamanhoEsperado
     */
    @Test(dataProvider = "lancamentos")
    public void getResultadoVOTest(List<Lancamento> lancamentos, String itemBusca, long tamanhoEsperado) {
        when(lancamentoService.busca(itemBusca)).thenReturn(lancamentos);
        when(lancamentoService.conta(itemBusca)).thenReturn((long) lancamentos.size());
        when(lancamentoService.getResultadoVO(lancamentos, this.tamanhoPagina, lancamentos.size())).thenCallRealMethod();
        given(lancamentoService.buscaAjax(itemBusca)).willCallRealMethod();
        ResultadoVO resultado = lancamentoService.buscaAjax(itemBusca);
        for (LancamentoVO lancamentoVO : resultado.getLancamentos()) {
            assertTrue(doesObjectContainField(lancamentoVO, "id") & !Objects.isNull(lancamentoVO.getId()));
            assertTrue(doesObjectContainField(lancamentoVO, "descricao") & lancamentoVO.getDescricao() != null);
            assertTrue(doesObjectContainField(lancamentoVO, "valor") & lancamentoVO.getValor() != null);
            assertTrue(doesObjectContainField(lancamentoVO, "dataLancamento") & lancamentoVO.getDataLancamento() != null);
            assertTrue(doesObjectContainField(lancamentoVO, "tipoLancamento") && lancamentoVO.getTipoLancamento() != null);
        }
        verify(lancamentoService, atLeastOnce()).buscaAjax(itemBusca);
    }

    /**
     * Método que busca em uma class por um atributo específico (privado)
     *
     * @param object    objeto a ser verificar
     * @param fieldName nome do campo a ser verificado
     * @return true se encontrado, e false caso contrário
     */
    public boolean doesObjectContainField(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getDeclaredFields()).anyMatch(f -> f.getName().equals(fieldName));
    }

    /**
     * Classe LancamentoBuilder
     * Responsável por construir um objeto da classe Lancamento usado no DataProvider
     */
    static class LancamentoBuilder {
        private Lancamento lancamento;

        LancamentoBuilder() {
            lancamento = new Lancamento();
        }

        LancamentoBuilder comData(String data) {
            StringToDateConverter stringToDate = new StringToDateConverter();
            lancamento.setDataLancamento(stringToDate.convert(data));
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

        Lancamento build() {
            return lancamento;
        }
    }
}
