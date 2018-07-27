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
import org.testng.ITestContext;
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

    @BeforeClass(alwaysRun = true)
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * DataProvider de lançamentos que devolve uma lista com tamanho baseado na
     * quantidade encontrada no método que o chama
     *
     * @param method método que chama o provider
     * @return lista de lançamentos para testes
     */
    @DataProvider(name = "lancamentos")
    protected Object[][] getLancamentos(Method method) {
        List<Lancamento> lancamentosCenario = new ArrayList<>();
        for (int i = 0; i < getQtdeLancamentosCenario(method.getName()); i++) {
            lancamentosCenario.add(new LancamentoBuilder().random().build());
        }
        return new Object[][]{
                new Object[]{lancamentosCenario}
        };
    }

    /**
     * Método que devolve a quantidade de lançamentos esperada baseada no número do método de teste
     *
     * @param method nome do método de testes
     * @return quantidade de lançamentos do cenário de teste
     */
    private int getQtdeLancamentosCenario(String method) {
        return Integer.parseInt(method.replaceAll("\\D+", ""));
    }

    /**
     * Teste com cenário com 10 lançamentos
     * @param lancamentos
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Test(dataProvider = "lancamentos", groups = "cenario10")
    public void buscaAjax10LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 10);
    }

    /**
     * Teste com cenário com 9 lançamentos
     * @param lancamentos
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Test(dataProvider = "lancamentos", groups = "cenario9")
    public void buscaAjax9LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 9);
    }

    /**
     * Teste com cenário com 3 lançamentos
     * @param lancamentos
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Test(dataProvider = "lancamentos", groups = "cenario3")
    public void buscaAjax3LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 3);
    }

    /**
     * Teste com cenário com 1 lançamento
     * @param lancamentos
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Test(dataProvider = "lancamentos", groups = "cenario1")
    public void buscaAjax1LancamentosTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 1);
    }

    /**
     * Teste com cenário com 0 lançamento
     * @param lancamentos
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Test(dataProvider = "lancamentos", groups = "cenario0")
    public void busca0LancamentosAjaxTest(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 0);
    }

    /**
     * Método que realiza os testes necessários do buscaAjax da classe LancamentoService
     *
     * @param lancamentos     lista de lançamentos gerada
     * @param tamanhoEsperado tamanho da lista esperada
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void buscaAjaxTest(List<Lancamento> lancamentos, long tamanhoEsperado) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        when(lancamentoService.getTotalEntrada(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalSaida(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.somaValoresPorTipo(anyListOf(Lancamento.class), any(TipoLancamento.class))).thenCallRealMethod();
        when(lancamentoService.getResultadoVO(anyListOf(Lancamento.class), anyInt(), anyLong())).thenCallRealMethod();
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        given(lancamentoService.buscaAjax(anyString())).willCallRealMethod();

        /**
         * Validação do valor total de entradas na lista de lançamentos
         */
        BigDecimal totalEntradaEsperado = this.getTotalPorTipo(lancamentos, TipoLancamento.ENTRADA);
        BigDecimal totalEntradaObtido = lancamentoService.getTotalEntrada(lancamentos);
        assertEquals(totalEntradaObtido, totalEntradaEsperado, "Era esperado o total de entrade de " + totalEntradaEsperado.toString() + ", mas foi obtido um valor de " + totalEntradaObtido.toString());
        /**
         *  Validação do valor total de saídas na lista de lançamentos
         */
        BigDecimal totalSaidaEsperado = this.getTotalPorTipo(lancamentos, TipoLancamento.SAIDA);
        BigDecimal totalSaidadaObtido = lancamentoService.getTotalSaida(lancamentos);
        assertEquals(totalSaidadaObtido, totalSaidaEsperado, "Era esperado o total de sáida de " + totalSaidaEsperado.toString() + ", mas foi obtido um valor de " + totalSaidadaObtido.toString());

        /**
         * Validação do tamanho da lista de lançamentos
         */
        long tamanhoObtido = lancamentoService.buscaAjax(anyString()).getTotalRegistros();
        assertEquals(tamanhoObtido, tamanhoEsperado, "Era esperado o tamanho da lista de lançamentos de " + tamanhoEsperado + " registro(s), mas foi retornado " + tamanhoObtido + " registro(s).");
        /**
         * Validação para garantir que todo atributo da classe Lancamento esteja na classe
         *  LancamentoVO e não nula
         */
        final ResultadoVO resultadoVO = lancamentoService.buscaAjax(anyString());
        // Obtém a lista de atributos da classe Lancamento
        Field[] campos = Lancamento.class.getDeclaredFields();
        //Percorre a lista de resultado da busca para fazer as validações
        for (LancamentoVO lancamentoVO : resultadoVO.getLancamentos()) {
            for (Field campo : campos) {
                String atributo = campo.getName();
                // Verifica se cada atributo da classe lançamento está na classe LancamentoVO
                assertTrue(doesObjectContainField(lancamentoVO, atributo), "Atributo " + atributo + " não existe na Classe LancamentoVO.");
                //Verificar se cada atributo não está vazio
                String valorAtributo = getMethodValue(lancamentoVO, atributo).toString();
                assertTrue(!valorAtributo.equals("") && valorAtributo != null, "Atributo " + atributo + " é nulo na Classe Lancamento VO.");
            }
        }
    }

    /**
     * Método que calcula o total por tipo de lançamento da lista gerada aleatoriament
     *
     * @param lancamentos lista de lançamentos do teste atual
     * @param tipo        tipo de lançamento(entrada ou saída)
     * @return soma dos valores encontrados do tipo na lista
     */
    private BigDecimal getTotalPorTipo(List<Lancamento> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(l -> l.getTipoLancamento() == tipo)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Método que invoca o método get do atributo informado de um classe
     *
     * @param object   objeto a ser verificado
     * @param atributo atributo a ser buscado seu respectivo método get
     * @return valor do atributo
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Object getMethodValue(Object object, String atributo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            String getMethodtext = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
            Method getMethod = object.getClass().getMethod(getMethodtext, new Class[]{});
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
     * Método que busca em uma classe por um atributo específico (privado)
     *
     * @param object    objeto a ser verificado
     * @param fieldName nome do campo a ser verificado
     * @return true se encontrado, e false caso contrário
     */
    private boolean doesObjectContainField(Object object, String fieldName) {
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

        /**
         * Método que gera o objeto Lancamento de forma aleatória
         *
         * @return
         */
        LancamentoBuilder random() {
            Random rand = new Random();
            return comId(rand.nextLong())
                    .comData(rand.nextInt(30), rand.nextInt(7), 2018)
                    .comTipo(rand.nextBoolean() ? TipoLancamento.ENTRADA : TipoLancamento.SAIDA)
                    .comDescricao("Lançamento " + rand.nextInt())
                    .comValor(rand.nextInt(100_000) / 100.0);
        }

        Lancamento build() {
            return lancamento;
        }
    }
}
