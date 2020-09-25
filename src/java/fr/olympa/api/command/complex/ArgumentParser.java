package fr.olympa.api.command.complex;

import java.util.function.Function;

public class ArgumentParser {

	public Function<String, Object> supplyArgumentFunction;
	public Function<String, String> wrongArgTypeMessageFunction;

	/**
	 * @Deprecated
	 *
	 * Il est possible d'utiliser le tabArgumentsFunction pour vérifier le type de l'arguement.
	 * Il suffit de mettre le message d'erreur dans errorMessageArgumentFunction plutôt que dans tabArgumentsFunction sinon le message d'erreur sera envoyé avec que le plugin le gère.
	 *
	 * Utilise plutôt {@link #ComplexUtils(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction) ComplexUtils}.
	 */
	@Deprecated(forRemoval = true)
	protected ArgumentParser(Function<String, Object> supplyArgumentFunction) {
		this.supplyArgumentFunction = supplyArgumentFunction;
	}

	/**
	 *
	 * @param tabArgumentsFunction
	 * @param supplyArgumentFunction
	 * @param wrongArgTypeMessageFunction Le message ne doit pas finir par un point, et doit avoir un sens en utiliser le message suivie d'un ou (ex: Ton message d'erreur OU un autre message d'erreur)
	 */
	protected ArgumentParser(Function<String, Object> supplyArgumentFunction, Function<String, String> wrongArgTypeMessageFunction) {
		this.supplyArgumentFunction = supplyArgumentFunction;
		this.wrongArgTypeMessageFunction = wrongArgTypeMessageFunction;
	}

}
