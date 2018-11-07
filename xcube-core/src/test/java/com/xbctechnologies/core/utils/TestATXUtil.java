package com.xbctechnologies.core.utils;

import org.junit.Test;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class TestATXUtil {
    @Test
    public void generateXTO() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        BigInteger value = CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, 10);
        System.out.println(formatter.format(value));
    }

    @Test
    public void test() {
        //16,999,999,999,999,992,375,001
        BigInteger reward = new BigInteger("999,999,999,999,992,375,001".replaceAll(",", ""));
        BigInteger unit = reward.divide(new BigInteger("100"));
        BigInteger validatorReward = unit.multiply(new BigInteger("30"));
        BigInteger delegatorReward = unit.multiply(new BigInteger("70"));
        BigInteger unitDelegator = delegatorReward.divide(new BigInteger("8000001"));
        BigInteger one = unitDelegator.multiply(new BigInteger("8000000"));
        BigInteger two = unitDelegator.multiply(new BigInteger("1"));
        System.out.println("unit : " + String.format("%,d", unit));
        System.out.println("validatorReward :" + String.format("%,d", validatorReward));
        System.out.println("delegatorReward :" + String.format("%,d", delegatorReward));
        System.out.println("sum : " + String.format("%,d", validatorReward.add(delegatorReward)));

        System.out.println();

        System.out.println("unitDelegator :" + String.format("%,d", unitDelegator));
        System.out.println("one :" + String.format("%,d", one));
        System.out.println("two :" + String.format("%,d", two));
        System.out.println("sumDelegator :" + String.format("%,d", one.add(two)));
        System.out.println();
        //999,999,999,999,992,375,001
        //999,999,999,999,992,375,000

        BigInteger origin = new BigInteger("16999999999999992375001");
        BigInteger real = new BigInteger("16999999999999986775000");
        System.out.println(String.format("%,d", delegatorReward.subtract(one.add(two))));
        System.out.println(String.format("%,d", origin.subtract(real)));

//        unit : 9,999,999,999,999,923,750
//        validatorReward :299,999,999,999,997,712,500
//        delegatorReward :699,999,999,999,994,662,500
//        sum : 999,999,999,999,992,375,000
//
//        unitDelegator :87,499,989,062,500
//        one :699,999,912,500,000,000,000
//        two :87,499,989,062,500
//        sumDelegator :699,999,999,999,989,062,500
    }

    @Test
    public void comma() {
        BigInteger reward = new BigInteger("1000999999999993600000");
        BigInteger bonding = new BigInteger("7300000");
        BigInteger participantUnit = reward.divide(new BigInteger("100"));
        BigInteger rewardOfValidators = participantUnit.multiply(new BigInteger("30"));
        BigInteger rewardOfDelegators = participantUnit.multiply(new BigInteger("70"));

        BigInteger rewardUnitForValidator = new BigInteger(rewardOfValidators.toString());
        rewardUnitForValidator = rewardUnitForValidator.divide(bonding);

        BigInteger rewardUnitForDelegator = new BigInteger(rewardOfDelegators.toString());
        rewardUnitForDelegator = rewardUnitForDelegator.divide(bonding);

        //For reward of delegator
        System.out.println(rewardUnitForDelegator.multiply(new BigInteger("10")));
        System.out.println(reward.subtract(rewardUnitForDelegator.multiply(new BigInteger("10"))));
    }

    @Test
    public void ccc() {
        //9,014,400,125,199,980,550,019
        BigInteger origin = new BigInteger("8000001".replaceAll(",", ""));
        BigInteger added = new BigInteger("10000,000,000,000,000,000,000".replaceAll(",", ""));
        BigInteger unit = added.divide(origin);
        System.out.println(unit);
        BigInteger last = unit.multiply(origin);
        System.out.println(last);
        unit = last.divide(new BigInteger("8000001".replaceAll(",", "")));
        System.out.println(last.subtract(unit.multiply(new BigInteger("800000"))));
    }

    @Test
    public void dfdsa() {
        BigInteger sum = new BigInteger("9014400125199980550019");
        BigInteger bonding1 = new BigInteger("7200000").multiply(new BigInteger("21")).multiply(new BigInteger("1000000000"));
        BigInteger bonding2 = new BigInteger("1").multiply(new BigInteger("5")).multiply(new BigInteger("1000000000"));
        BigInteger bonding3 = new BigInteger("99999").multiply(new BigInteger("5")).multiply(new BigInteger("1000000000"));

        //9014522525200980550019
        //9014522525216980550019

        System.out.println(bonding1.add(bonding2).add(bonding3));


        //993601506843101369
        //151700000000000000
        BigInteger sub = new BigInteger("2009017537726710808526806");
        System.out.println(sub.subtract(new BigInteger("2009016544125203965425437")));
    }

    @Test
    public void ddd() {
        BigInteger expectedReward = CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("11,000"));
        BigInteger bondingBalance = CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("8,000,001"));

        BigInteger unitForReward = expectedReward.divide(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondingBalance));
        BigInteger tempTotalReward = unitForReward.multiply(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondingBalance));

        BigInteger rewardUnitForParticipant = new BigInteger(tempTotalReward.toString()).divide(new BigInteger("100"));
        BigInteger expectedValidatorReward = new BigInteger(rewardUnitForParticipant.toString()).multiply(new BigInteger("30"));
        BigInteger expectedDelegatorReward = new BigInteger(rewardUnitForParticipant.toString()).multiply(new BigInteger("70"));

        BigInteger rewardUnitForValidator = expectedValidatorReward.divide(NumberUtil.generateStringToBigInteger("8,000,000"));
        BigInteger rewardValidator = rewardUnitForValidator.multiply(NumberUtil.generateStringToBigInteger("800,000"));

        BigInteger rewardUnitForDelegator = expectedDelegatorReward.divide(NumberUtil.generateStringToBigInteger("8,000,001"));
        BigInteger rewardDelegator = rewardUnitForDelegator.multiply(NumberUtil.generateStringToBigInteger("800,000"));

        System.out.println(NumberUtil.comma(expectedValidatorReward));
        System.out.println(NumberUtil.comma(expectedDelegatorReward));
        System.out.println(NumberUtil.comma(rewardValidator));
        System.out.println(NumberUtil.comma(rewardDelegator));
        System.out.println(NumberUtil.comma(expectedValidatorReward.subtract(rewardValidator).add(rewardValidator)));
        System.out.println(NumberUtil.comma(expectedDelegatorReward.subtract(rewardDelegator).add(rewardDelegator)));
        System.out.println(NumberUtil.comma(expectedReward.subtract(expectedValidatorReward).subtract(expectedDelegatorReward)));
    }

    @Test
    public void eee() {
        BigInteger expectedReward = CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("11,000"));
        BigInteger bondingBalance = CurrencyUtil.generateXTO(CurrencyUtil.CurrencyType.CoinType, NumberUtil.generateStringToBigInteger("8,000,001"));

        BigInteger unitForReward = expectedReward.divide(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondingBalance));
        BigInteger tempTotalReward = unitForReward.multiply(CurrencyUtil.generateCurrencyUnitToCurrencyUnit(CurrencyUtil.CurrencyType.XTOType, CurrencyUtil.CurrencyType.CoinType, bondingBalance));

        BigInteger rewardUnitForParticipant = new BigInteger(tempTotalReward.toString()).divide(new BigInteger("100"));
        BigInteger expectedValidatorReward = new BigInteger(rewardUnitForParticipant.toString()).multiply(new BigInteger("30"));
        BigInteger expectedDelegatorReward = new BigInteger(rewardUnitForParticipant.toString()).multiply(new BigInteger("70"));

        BigInteger rewardUnitForValidator = expectedValidatorReward.divide(NumberUtil.generateStringToBigInteger("8,000,001"));
        BigInteger rewardValidator = rewardUnitForValidator.multiply(NumberUtil.generateStringToBigInteger("800,000"));

        BigInteger rewardUnitForDelegator = expectedDelegatorReward.divide(NumberUtil.generateStringToBigInteger("8,000,001"));
        BigInteger rewardDelegator = rewardUnitForDelegator.multiply(NumberUtil.generateStringToBigInteger("800,000"));

        System.out.println("Validator의 보상값 : " + NumberUtil.comma(expectedValidatorReward));
        System.out.println("Delegator의 보상값 : " + NumberUtil.comma(expectedDelegatorReward));
        System.out.println("Unbonding시 Validator의 보상값 : " + NumberUtil.comma(rewardValidator));
        System.out.println("Unbonding시 Delegator의 보상값 : " + NumberUtil.comma(rewardDelegator));
        System.out.println("Unbonding시 Validator의 보상후 남은값 : " + NumberUtil.comma(expectedValidatorReward.subtract(rewardValidator)));
        System.out.println("Unbonding시 Delegator의 보상후 남은값 : " + NumberUtil.comma(expectedDelegatorReward.subtract(rewardDelegator)));
        System.out.println("Validator 차이값 : " + NumberUtil.comma(expectedValidatorReward.subtract(rewardValidator.add(expectedValidatorReward.subtract(rewardValidator)))));
        System.out.println("Delegator 차이값 : " + NumberUtil.comma(expectedDelegatorReward.subtract(rewardDelegator.add(expectedDelegatorReward.subtract(rewardDelegator)))));

//        unbonding:0, blockNo:17, validator:3299999999999998837500, reward:329999999999999200000, remain:2969999999999999637500
//        unbonding:0, blockNo:17, delegator:7699999999999997287500, reward:769999903750011200000, remain:6930000096249986087500
        System.out.println(expectedValidatorReward.compareTo(expectedValidatorReward.subtract(rewardValidator).add(rewardValidator)));
        System.out.println(expectedDelegatorReward.compareTo(expectedDelegatorReward.subtract(rewardDelegator).add(rewardDelegator)));
        System.out.println(NumberUtil.comma(expectedValidatorReward.add(expectedDelegatorReward).add(new BigInteger("3875000"))));
    }

    @Test
    public void fff() {
        BigInteger a = new BigInteger("6930000096249986087500".replaceAll(",", ""));
        BigInteger c = a.divide(new BigInteger("7200000"));


        a = new BigInteger("6930000096249986087500".replaceAll(",", ""));
//        c = a.divide(new BigInteger("7200000"));
//        BigInteger sub2 = a.subtract(c.multiply(new BigInteger("7200000")));

        //2399994
        //4487500

        System.out.println(a.subtract(c.multiply(new BigInteger("7200000"))));

        a = new BigInteger("6930000096249986087500".replaceAll(",", ""));
        c = new BigInteger("6929999133750100800000".replaceAll(",", ""));
        System.out.println(a.subtract(c));


//        BigInteger b = new BigInteger("22615399077680794142424");
//        System.out.println(NumberUtil.comma(a.subtract(b)));
//        System.out.println(NumberUtil.comma(a));
//        System.out.println(NumberUtil.comma(b));
//        System.out.println(NumberUtil.comma(new BigInteger("55013999999999999988125020")));
    }
}
