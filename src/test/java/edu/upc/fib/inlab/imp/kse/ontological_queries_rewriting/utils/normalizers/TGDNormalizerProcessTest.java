package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.normalizers;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.mothers.TGDMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.Literal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.upc.fib.inlab.imp.kse.logics.logicschema.assertions.LogicSchemaAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;


class TGDNormalizerProcessTest {

    @Nested
    class NormalizeSetOfTGDs {

        static Stream<Arguments> provideTGDsToNormalize() {
            return Stream.of(
                    Arguments.of(
                            List.of(
                                    "P(x) -> A1(x), A2(x, y, z)",
                                    "Q(x) -> A3(x), A4(x, y, z)"
                            ),
                            Set.of(
                                    "P(x) -> AUX3(x, y)",
                                    "AUX3(x, y) -> AUX4(x, y, z)",
                                    "AUX4(x, y, z) -> AUX1(x, y, z)",
                                    "AUX1(x, y, z) -> A1(x)",
                                    "AUX1(x, y, z) -> A2(x, y, z)",
                                    "Q(x) -> AUX5(x, y)",
                                    "AUX5(x, y) -> AUX6(x, y, z)",
                                    "AUX6(x, y, z) -> AUX2(x, y, z)",
                                    "AUX2(x, y, z) -> A3(x)",
                                    "AUX2(x, y, z) -> A4(x, y, z)"
                            )
                    ),
                    Arguments.of(
                            List.of(
                                    "P(x) -> A1(x), A2(x, y, z)",
                                    "Q(x), AUX1(x) -> A3(x), A4(x, y, z)"
                            ),
                            Set.of(
                                    "P(x) -> AUX4(x, y)",
                                    "AUX4(x, y) -> AUX5(x, y, z)",
                                    "AUX5(x, y, z) -> AUX2(x, y, z)",
                                    "AUX2(x, y, z) -> A1(x)",
                                    "AUX2(x, y, z) -> A2(x, y, z)",
                                    "Q(x), AUX1(x) -> AUX6(x, y)",
                                    "AUX6(x, y) -> AUX7(x, y, z)",
                                    "AUX7(x, y, z) -> AUX3(x, y, z)",
                                    "AUX3(x, y, z) -> A3(x)",
                                    "AUX3(x, y, z) -> A4(x, y, z)"
                            )
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("provideTGDsToNormalize")
        void should_return_TGDs_normalized(List<String> originalsTGDsString, Set<String> expectedTGDsString) {
            Set<TGD> originalTGDs = originalsTGDsString.stream()
                    .map(TGDMother::createTGD)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            TGDNormalizerProcess tgdNormalizer = new TGDNormalizerProcess();

            Set<TGD> normalizedTGD = tgdNormalizer.normalize(originalTGDs);

            Set<TGD> expectedTGDs = expectedTGDsString.stream().map(TGDMother::createTGD).collect(Collectors.toSet());

            assertThat(normalizedTGD)
                    .usingElementComparator(tgdComparator())
                    .containsExactlyInAnyOrderElementsOf(expectedTGDs);
        }

    }

    @Nested
    class OneTGD {

        @Nested
        class NormalizeOneTGD {

            static Stream<Arguments> provideSingleTGDsToNormalize() {
                return Stream.of(
                        Arguments.of(
                                "P(x) -> A1(x), A2(x, y, z)",
                                Set.of(
                                        "P(x) -> AUX2(x, y)",
                                        "AUX1(x, y, z) -> A1(x)",
                                        "AUX1(x, y, z) -> A2(x, y, z)",
                                        "AUX2(x, y) -> AUX3(x, y, z)",
                                        "AUX3(x, y, z) -> AUX1(x, y, z)"
                                )
                        ),
                        Arguments.of(
                                "P(x) -> A1(x, y), A2(y, z)",
                                Set.of(
                                        "P(x) -> AUX2(x, y)",
                                        "AUX2(x, y) -> AUX3(x, y, z)",
                                        "AUX3(x, y, z) -> AUX1(x, y, z)",
                                        "AUX1(x, y, z) -> A1(x, y)",
                                        "AUX1(x, y, z) -> A2(y, z)"
                                )
                        )
                );
            }

            @ParameterizedTest
            @MethodSource("provideSingleTGDsToNormalize")
            void should_return_TGDs_normalized(String tgdString, Set<String> expectedTGDsString) {
                TGD tgd = TGDMother.createTGD(tgdString);
                TGDNormalizerProcess tgdNormalizer = new TGDNormalizerProcess();

                Set<TGD> normalizedTGD = tgdNormalizer.normalize(Set.of(tgd));

                Set<TGD> expectedTGDs = expectedTGDsString.stream().map(TGDMother::createTGD).collect(Collectors.toSet());

                assertThat(normalizedTGD)
                        .usingElementComparator(tgdComparator())
                        .containsExactlyInAnyOrderElementsOf(expectedTGDs);
            }

        }

        @Nested
        class NormalizeOneTGD_To_OneHeadAtom {

            static Stream<Arguments> provideTGDsToNormalizeToOneHeadAtom() {
                return Stream.of(
                        Arguments.of(
                                "P(x) -> A1(x)",
                                Set.of(
                                        "P(x) -> A1(x)"
                                )
                        ),
                        Arguments.of(
                                "P(x) -> A1(x), A2(x)",
                                Set.of(
                                        "P(x) -> AUX1(x)",
                                        "AUX1(x) -> A1(x)",
                                        "AUX1(x) -> A2(x)"
                                )
                        ),
                        Arguments.of(
                                "P(x), Q(x) -> A1(x), A2(y)",
                                Set.of(
                                        "P(x), Q(x) -> AUX1(x, y)",
                                        "AUX1(x, y) -> A1(x)",
                                        "AUX1(x, y) -> A2(y)"
                                )
                        ),
                        Arguments.of(
                                "P(x), AUX1(x) -> A1(x), A2(y)",
                                Set.of(
                                        "P(x), AUX1(x) -> AUX2(x, y)",
                                        "AUX2(x, y) -> A1(x)",
                                        "AUX2(x, y) -> A2(y)"
                                )
                        )
                );
            }

            @ParameterizedTest
            @MethodSource("provideTGDsToNormalizeToOneHeadAtom")
            void should_return_TGDs_only_oneHeadAtom(String tgdString, Set<String> expectedTGDsString) {
                TGD tgd = TGDMother.createTGD(tgdString);
                TGDNormalizerProcess tgdNormalizer = new TGDNormalizerProcess();

                Set<TGD> normalizedTGD = tgdNormalizer.normalize(Set.of(tgd));

                Set<TGD> expectedTGDs = expectedTGDsString.stream().map(TGDMother::createTGD).collect(Collectors.toSet());

                assertThat(normalizedTGD)
                        .usingElementComparator(tgdComparator())
                        .containsExactlyInAnyOrderElementsOf(expectedTGDs);
            }

        }

        @Nested
        class NormalizeOneTGD_ExistentiallyQuantifiedVariables {

            static Stream<Arguments> provideTGDsTONormalizeExistentiallyQuantifiedVariables() {
                return Stream.of(
                        Arguments.of(
                                "P(x) -> Q(x, y)",
                                Set.of(
                                        "P(x) -> Q(x, y)"
                                )
                        ),
                        Arguments.of(
                                "P(x), Q(y) -> R(x, y, z)",
                                Set.of(
                                        "P(x), Q(y) -> R(x, y, z)"
                                )
                        ),
                        Arguments.of(
                                "P(x) -> Q(x, y, z)",
                                Set.of(
                                        "P(x) -> AUX1(x, y)",
                                        "AUX1(x, y) -> AUX2(x, y, z)",
                                        "AUX2(x, y, z) -> Q(x, y, z)"
                                )
                        )
                );
            }

            @ParameterizedTest
            @MethodSource("provideTGDsTONormalizeExistentiallyQuantifiedVariables")
            void should_return_TGDs_only_oneExistentiallyQuantifiedVariable_when_TGD_containsSeveralExistentiallyQuantifiedVariables(String tgdString, Set<String> expectedTGDsString) {
                TGD tgd = TGDMother.createTGD(tgdString);
                TGDNormalizerProcess tgdNormalizer = new TGDNormalizerProcess();

                Set<TGD> normalizedTGD = tgdNormalizer.normalize(Set.of(tgd));

                Set<TGD> expectedTGDs = expectedTGDsString.stream().map(TGDMother::createTGD).collect(Collectors.toSet());

                assertThat(normalizedTGD)
                        .usingElementComparator(tgdComparator())
                        .containsExactlyInAnyOrderElementsOf(expectedTGDs);
            }
        }

    }

    private Comparator<TGD> tgdComparator() {
        return (tgd1, tgd2) -> {
            try {
                assertThat(tgd1.getHead())
                        .containsAtomsByPredicateName(tgd2.getHead());
            } catch (AssertionError e) {
                return -1;
            }
            try {
                List<String> tgd2BodyLiteralAsStringList = tgd2.getBody().stream()
                        .map(Literal::toString)
                        .toList();
                assertThat(tgd1.getBody())
                        .hasSize(tgd2.getBody().size())
                        .containsExactlyLiteralsOf(tgd2BodyLiteralAsStringList);
            } catch (AssertionError e) {
                return 1;
            }
            return 0;
        };
    }

}