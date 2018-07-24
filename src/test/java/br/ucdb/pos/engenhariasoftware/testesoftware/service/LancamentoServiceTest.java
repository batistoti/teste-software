package br.ucdb.pos.engenhariasoftware.testesoftware.service;

import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.LancamentoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.ResultadoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.converter.StringToDateConverter;
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
import org.testng.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento.ENTRADA;
import static br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento.SAIDA;
import static br.ucdb.pos.engenhariasoftware.testesoftware.util.Constantes.DD_MM_YYYY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class LancamentoServiceTest {
    @Mock
    private LancamentoService lancamentoService;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        given(lancamentoService.buscaAjax(anyString())).willCallRealMethod();
        when(lancamentoService.getTotalEntrada(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalSaida(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.somaValoresPorTipo(anyListOf(Lancamento.class), any(TipoLancamento.class))).thenCallRealMethod();
        when(lancamentoService.getResultadoVO(anyListOf(Lancamento.class), eq(10), anyLong())).thenCallRealMethod();
    }

    @DataProvider(name = "lancamentos")
    protected Object[][] getLancamentos() {
        return new Object[][]{
                new Object[]{
                        Arrays.asList(
                                new LancamentoBuilder().comId((long) 1).comDescricao("Salário").comData("05/07/2018").comValor(5000.00).comTipo(ENTRADA).build(),
                                new LancamentoBuilder().comId((long) 2).comDescricao("Benefícios").comData("05/07/2018").comValor(170.00).comTipo(ENTRADA).build(),
                                new LancamentoBuilder().comId((long) 3).comDescricao("Academia").comData("05/07/2018").comValor(75.00).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 4).comDescricao("Parcela Carro").comData("05/07/2018").comValor(1227.00).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 5).comDescricao("Cartão Nubank").comData("06/07/2018").comValor(876.78).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 6).comDescricao("Cartão Submarino").comData("06/07/2018").comValor(455.78).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 7).comDescricao("Parcela Carro").comData("05/07/2018").comValor(455.78).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 8).comDescricao("Pós-Graduação").comData("10/07/2018").comValor(296.88).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 9).comDescricao("Abastecimento").comData("08/07/2018").comValor(120.00).comTipo(SAIDA).build(),
                                new LancamentoBuilder().comId((long) 10).comDescricao("").comData("13/07/2018").comValor(94.00).comTipo(SAIDA).build()
                        )
                }
        };
    }


    /**
     * Método de teste responsável por validar o total de entradas da lista de lançamentos
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    @Test(dataProvider = "lancamentos")
    public void getTotalEntradaTest(List<Lancamento> lancamentos) {
        final BigDecimal totalEsperado = BigDecimal.valueOf(5170.00);
        assertEquals(lancamentoService.getTotalEntrada(lancamentos), totalEsperado);
        verify(lancamentoService, atLeastOnce()).getTotalEntrada(anyListOf(Lancamento.class));
    }

    /**
     * Método de teste responsável por validar o total de entradas da lista de lançamentos
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    @Test(dataProvider = "lancamentos")
    public void getTotalSaidaTest(List<Lancamento> lancamentos) {
        final BigDecimal totalEsperado = BigDecimal.valueOf(3601.22);
        final BigDecimal totalObtido = lancamentoService.getTotalSaida(lancamentos);
        assertEquals(totalObtido, totalEsperado);
        verify(lancamentoService, atLeastOnce()).getTotalSaida(anyListOf(Lancamento.class));
    }

    /**
     * Teste responsável por validar o tamanho da lista de lançamentos
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    @Test(dataProvider = "lancamentos")
    public void getTamanhoListaTest(List<Lancamento> lancamentos) {
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        long tamanhoEsperado = 10;
        long tamanhoObtido = lancamentoService.buscaAjax(anyString()).getTotalRegistros();
        String mensagem = "Era espero o tamanho da lista de lançamentos de " + tamanhoEsperado + " registro(s), mas foi retornado " + tamanhoObtido + " registro(s).";
        assertEquals(tamanhoObtido, tamanhoEsperado, mensagem);
        verify(lancamentoService, atLeastOnce()).buscaAjax(anyString());
    }

    /**
     * Método de teste responsával por garantir que todo atributo da classe Lancamento esteja na LancamentoVO e não nula
     *
     * @param lancamentos lista de lançamento criada para o teste
     */
    @Test(dataProvider = "lancamentos")
    public void getResultadoVOTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        ResultadoVO resultadoVO = lancamentoService.buscaAjax(anyString());
        // Obtém a lista de atributos da classe Lancamento
        Field[] campos = Lancamento.class.getDeclaredFields();
        //Percorre a lista de resultado da busca para fazer as validações
        for (LancamentoVO lancamentoVO : resultadoVO.getLancamentos()) {
            for (Field campo : campos) {
                String atributo = campo.getName();
                // Verifica se cada atributo da classe lançamento está na classe LancamentoVO
                assertTrue(doesObjectContainField(lancamentoVO, atributo), "Atributo " + atributo + " não existe na Classe LancamentoVO.");
                //Verificar se cada atributo não está vazio
                assertNotNull(getMethodValue(lancamentoVO, atributo), "Atributo " + atributo + " é nulo na Classe Lancamento VO.");

            }
        }
        verify(lancamentoService, atLeastOnce()).buscaAjax(anyString());
    }

    /**
     * Método que invoca o método get do atributo informado de um classe
     *
     * @param object
     * @param atributo
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object getMethodValue(Object object, String atributo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getMethod = null;
        try {
            String getMethodtext = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
            getMethod = object.getClass().getMethod(getMethodtext, new Class[]{});
            return getMethod.invoke(object, new Object[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Método que busca em uma class por um atributo específico (privado)
     *
     * @param object    objeto a ser verificado
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

        LancamentoBuilder comId(Long id) {
            lancamento.setId(id);
            return this;
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
