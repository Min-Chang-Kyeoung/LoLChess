package com.lolchess.strategy.view.menu


import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
//import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.lolchess.strategy.R
import com.lolchess.strategy.controller.database.SimulatorDB
import com.lolchess.strategy.controller.entity.SimulatorChamp
import com.lolchess.strategy.controller.entity.SimulatorSynergy
import com.lolchess.strategy.controller.viewmodel.SimualtorViewModel
import com.lolchess.strategy.model.data.ChampData
import com.lolchess.strategy.model.Champ
import com.lolchess.strategy.model.data.SynergyData
import com.lolchess.strategy.view.adapter.ChampMainAdapter
import com.lolchess.strategy.view.adapter.SimulationAdapter
import com.lolchess.strategy.view.adapter.SimulationSynergyAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.android.synthetic.main.simulator_fragment.*
import kotlinx.coroutines.GlobalScope


class Simulator : Fragment() {

    companion object {
        fun newInstance() = Simulator()
    }


    private lateinit var simulatorDB: SimulatorDB
    private lateinit var simulatorViewModel: SimualtorViewModel
    private lateinit var simulationAdapter: SimulationAdapter
    private lateinit var simAdapter: SimulationSynergyAdapter
   // private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.simulator_fragment, container, false)
        MobileAds.initialize(context)
        //createFrontAd()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        simulatorDB = SimulatorDB.getInstance(view.context)!!
        simulatorViewModel =
            ViewModelProvider(this, SimualtorViewModel.Factory(activity!!.application)).get(
                SimualtorViewModel::class.java
            )
        simulationAdapter = SimulationAdapter(view.context)
        simAdapter = SimulationSynergyAdapter(view?.context!!)

        /*lifecycleScope.launch(Dispatchers.IO) {
            simulatorDB?.SimulatorDAO().deleteAllChamp()
            simulatorDB?.SimulatorDAO().deleteAllSynergy()
        }
        */

        ///=> 챔프랑 시너지 삭제할때만 사용
        initAd()
        initChampView()
        initSimulation()
        initSimulationSynergy()
        initSearchBar()

    }

    override fun onDestroy() {
        //createFrontAd()
        super.onDestroy()
    }



   /* private fun createFrontAd() {

        mInterstitialAd = InterstitialAd(context)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.e("TAG", "The interstitial wasn't loaded yet.")
        }

    }*/

    private fun addChamp(champ: SimulatorChamp) {
        simulatorViewModel.insert(champ)
    }

    private fun addSynergy(synergy: SimulatorSynergy) {
        simulatorViewModel.insert(synergy)
    }

    private fun initAd() {
        val adBuilder = AdRequest.Builder().build()
        adView.loadAd(adBuilder)
    }

    private fun initSimulationSynergy() {

        sinergyView?.adapter = simAdapter
        simulatorViewModel.getAllSynergy().observe(viewLifecycleOwner, Observer { synergy ->
            synergy?.let { simAdapter.setData(synergy) }
            for(syn in synergy)
                Log.e("Synergy",syn.name + " " + syn.synPower.toString() )
        })

    }

    private fun initSimulation() {
        simulationAdapter.setItemClickListener(object : SimulationAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, champ: SimulatorChamp) {
                simulatorViewModel?.deleteChampByName(champ?.name)
                var synArr: Array<String>?

                if (champ?.thirdSynergy == "") {
                    synArr =
                        arrayOf(champ?.firstSynergy!!, champ?.secondSynergy!!)
                } else {
                    synArr =
                        arrayOf(
                            champ?.firstSynergy!!,
                            champ?.secondSynergy!!,
                            champ?.thirdSynergy!!
                        )
                }
                GlobalScope.launch(Dispatchers.Default) {
                    var simulatorSyn = simulatorViewModel?.getSynergyByName(synArr)
                    val synergymain: SynergyData = SynergyData()


                    for (syn in simulatorSyn) {
                        if (syn.count!! < 2) {
                            Log.e("syn1", syn.name + " " + syn.count.toString())
                            simulatorViewModel?.deleteSynergyByName(syn?.name)
                        } else {
                            Log.e("syn2", syn.name + " " + syn.count.toString())
                            syn.count = syn?.count!!.minus(1)
                            if ((syn.name.equals(synergymain.getBattlecast().name) && (syn.count!! >= 4 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getCelestial().name) && (syn.count!! >= 4 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getChrono().name) && (syn.count!! >= 4 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getDarkStar().name) && (syn.count!! >= 4 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getSorcerer().name) && (syn.count!! >= 4 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getVanguard().name) && (syn.count!! >= 4 && syn.count!!< 6))){
                                syn.synPower = 2
                            }


                            if ((syn.name.equals(synergymain.getSpacePirate().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getBlaster().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getBrawler().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getBattlecast().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getBlademaster().name) && (syn.count!! >= 3 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getCybernetic().name) && (syn.count!! >= 3 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getCelestial().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getChrono().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getDarkStar().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getInfiltrator().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getMystic().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getProtector().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getRebel().name) && (syn.count!! >= 3 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getSorcerer().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getStarGuardian().name) && (syn.count!! >= 3 && syn.count!!< 6))
                                    ||(syn.name.equals(synergymain.getSniper().name) && (syn.count!! >= 2 && syn.count!!< 4))
                                    ||(syn.name.equals(synergymain.getVanguard().name) && (syn.count!! >= 2 && syn.count!!< 4))){
                                syn.synPower = 1
                            }

                            if ((syn.name.equals(synergymain.getMechPilot().name) && (syn.count!!< 3))
                                    ||(syn.name.equals(synergymain.getAstro().name) && (syn.count!!< 3))
                                    ||(syn.name.equals(synergymain.getSpacePirate().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getBlaster().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getBrawler().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getBattlecast().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getBlademaster().name) && (syn.count!! < 3))
                                    ||(syn.name.equals(synergymain.getCybernetic().name) && (syn.count!! < 3))
                                    ||(syn.name.equals(synergymain.getCelestial().name) && (syn.count!! < 2))
                                    ||(syn.name.equals(synergymain.getChrono().name) && (syn.count!! < 2))
                                    ||(syn.name.equals(synergymain.getDarkStar().name) && (syn.count!!< 2))
                                    ||(syn.name.equals(synergymain.getInfiltrator().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getMystic().name) && (syn.count!! < 2))
                                    ||(syn.name.equals(synergymain.getProtector().name) && (syn.count!!<2))
                                    ||(syn.name.equals(synergymain.getRebel().name) && (syn.count!! < 3))
                                    ||(syn.name.equals(synergymain.getSorcerer().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getStarGuardian().name) && (syn.count!! < 3))
                                    ||(syn.name.equals(synergymain.getSniper().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getVanguard().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getDemolitionist().name) && (syn.count!! <2))
                                    ||(syn.name.equals(synergymain.getManaReaver().name) && (syn.count!! <2))){
                                syn.synPower = 0
                            }


                            simulatorViewModel?.insert(syn)
                        }
                    }
                }
            }

        })
        simulationView?.adapter = simulationAdapter
        simulatorViewModel.getAllChamp().observe(viewLifecycleOwner, Observer { champs ->
            champs?.let { simulationAdapter.setData(champs) }
        })
    }

    private fun initChampView() {
        val champData = ChampData()
        val champList: List<Champ> =
            listOf(
                champData.getGraves(), champData.getNocturne(), champData.getLeona(), champData.getMalphite(), champData.getPoppy(), champData.getIllaoi(),
                champData.getJarvanIV(), champData.getXayah(), champData.getZoe(), champData.getZiggs(), champData.getCaitlyn(), champData.getTwistedFate(),
                champData.getFiora(), champData.getNautilus(), champData.getDarius(), champData.getRakan(), champData.getLucian(), champData.getMordekaiser(),
                champData.getBlitzcrank(), champData.getShen(), champData.getXinZhao(), champData.getAhri(), champData.getAnnie(), champData.getYasuo(),
                champData.getZed(), champData.getKogMaw(), champData.getNeeko(), champData.getRumble(), champData.getMasterYi(), champData.getBard(),
                champData.getVi(), champData.getVayne(), champData.getShaco(), champData.getSyndra(), champData.getAshe(), champData.getEzreal(),
                champData.getJayce(), champData.getKarma(), champData.getCassiopeia(), champData.getGnar(), champData.getRiven(), champData.getViktor(),
                champData.getSoraka(), champData.getWukong(), champData.getIrelia(), champData.getJhin(), champData.getJinx(), champData.getTeemo(),
                champData.getFizz(), champData.getGangplank(), champData.getLulu(), champData.getThresh(), champData.getAurelionSol(), champData.getEkko(),
                champData.getUrgot(), champData.getJanna(), champData.getXerath()
            )

        val champMutableList = champList.toMutableList()
        val mAdapter = ChampMainAdapter(view!!.context, champMutableList)

        mAdapter.setItemClickListener(object : ChampMainAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, champ: Champ) {
                var flag = false
                val synergymain: SynergyData = SynergyData()

                simulatorViewModel.getAllChamp().observe(viewLifecycleOwner, Observer { champs ->

                    if (champs.count() > 10) {
                        flag = true
                    }

                    for (chkChamp in champs) {
                        if (chkChamp.name == champ.name) {
                            flag = true
                        }
                    }
                })
                if (flag) {
                    return
                } else {

                    if (champ?.synergy?.size == 2) {
                        val simChamp = SimulatorChamp(
                            champ?.name,
                            champ?.imgPath,
                            champ?.cost,
                            champ?.synergy[0]?.name,
                            champ?.synergy[1].name,
                            ""
                        )

                        var firstSyn =
                            SimulatorSynergy(
                                champ?.synergy[0]?.name,
                                champ?.synergy[0]?.imgPath,
                                1
                            ,0
                            )
                        var secondSyn =
                            SimulatorSynergy(
                                champ?.synergy[1]?.name,
                                champ?.synergy[1]?.imgPath,
                                1
                            ,0

                            )


                        simulatorViewModel.getAllSynergy()
                            .observe(viewLifecycleOwner, Observer { synergy ->
                                synergy?.let { synergy ->
                                    for (syn in synergy) {
                                        if (firstSyn.name.equals(syn.name)) {
                                            //var power = getSynPower(syn)
                                            //syn.synPower = power
                                           // firstSyn.synPower = syn.synPower
                                            firstSyn.count = syn.count?.plus(1)

                                            Log.e("power1",  firstSyn.synPower.toString())
                                        }

                                        if (secondSyn.name.equals(syn.name)) {
                                            //var power = getSynPower(syn)
                                            secondSyn.count = syn.count?.plus(1)
                                           // secondSyn.synPower = power


                                            Log.e("power2",  secondSyn.synPower.toString())
                                        }
                                    }
                                }
                            })
                        if ((firstSyn.name.equals(synergymain.getSpacePirate().name) && (firstSyn.count == 2 || firstSyn.count == 3))
                                ||(firstSyn.name.equals(synergymain.getStarGuardian().name) && (firstSyn.count == 3 || firstSyn.count == 4 || firstSyn.count == 5))
                                ||(firstSyn.name.equals(synergymain.getCybernetic().name) && (firstSyn.count == 3 || firstSyn.count == 4 || firstSyn.count == 5))
                                ||(firstSyn.name.equals(synergymain.getChrono().name) && (firstSyn.count == 2 || firstSyn.count == 3))
                                ||(firstSyn.name.equals(synergymain.getBattlecast().name) && (firstSyn.count == 2 || firstSyn.count == 3))
                                ||(firstSyn.name.equals(synergymain.getRebel().name) && (firstSyn.count == 3 || firstSyn.count == 4 || firstSyn.count == 5))
                                ||(firstSyn.name.equals(synergymain.getDarkStar().name) && (firstSyn.count == 2 || firstSyn.count == 3))
                                ||(firstSyn.name.equals(synergymain.getCelestial().name) && (firstSyn.count == 2 || firstSyn.count == 3))) firstSyn.synPower = 1

                        if ((firstSyn.name.equals(synergymain.getChrono().name) && (firstSyn.count == 4 || firstSyn.count == 5))
                                ||(firstSyn.name.equals(synergymain.getBattlecast().name) && (firstSyn.count == 4 || firstSyn.count == 5))
                                ||(firstSyn.name.equals(synergymain.getDarkStar().name) && (firstSyn.count == 4 || firstSyn.count == 5))
                                ||(firstSyn.name.equals(synergymain.getCelestial().name) && (firstSyn.count == 4 || firstSyn.count == 5))) firstSyn.synPower = 2


                        if ((firstSyn.name.equals(synergymain.getChrono().name) && (firstSyn.count == 6 || firstSyn.count == 7 || firstSyn.count == 8))
                                ||(firstSyn.name.equals(synergymain.getBattlecast().name) && (firstSyn.count == 6))
                                ||(firstSyn.name.equals(synergymain.getDarkStar().name) && (firstSyn.count == 6))
                                ||(firstSyn.name.equals(synergymain.getRebel().name) && (firstSyn.count == 6 || firstSyn.count == 7))
                                ||(firstSyn.name.equals(synergymain.getMechPilot().name) && firstSyn.count == 3)
                                ||(firstSyn.name.equals(synergymain.getAstro().name) && (firstSyn.count == 3))
                                ||(firstSyn.name.equals(synergymain.getStarGuardian().name) && (firstSyn.count == 6 || firstSyn.count == 7))
                                ||(firstSyn.name.equals(synergymain.getCybernetic().name) && (firstSyn.count == 6 || firstSyn.count == 7))
                                ||(firstSyn.name.equals(synergymain.getSpacePirate().name) && (firstSyn.count == 4))) firstSyn.synPower = 3

                        if((secondSyn.name.equals(synergymain.getBlademaster().name) &&(secondSyn.count == 3 || secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getBrawler().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getBlaster().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getSorcerer().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getVanguard().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getProtector().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getMystic().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getInfiltrator().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getSniper().name) &&(secondSyn.count == 2 || secondSyn.count == 3))) secondSyn.synPower = 1

                        if((secondSyn.name.equals(synergymain.getVanguard().name) &&(secondSyn.count == 3 || secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getSorcerer().name) &&(secondSyn.count == 4 || secondSyn.count == 5))) secondSyn.synPower = 2

                        if((secondSyn.name.equals(synergymain.getBlademaster().name) &&(secondSyn.count == 6 || secondSyn.count == 7))
                                ||(secondSyn.name.equals(synergymain.getBrawler().name) &&(secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getBlaster().name) &&(secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getSorcerer().name) &&(secondSyn.count == 6 || secondSyn.count == 7))
                                ||(secondSyn.name.equals(synergymain.getVanguard().name) &&(secondSyn.count == 6))
                                ||(secondSyn.name.equals(synergymain.getProtector().name) &&(secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getMystic().name) &&(secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getInfiltrator().name) &&(secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getSniper().name) &&(secondSyn.count == 4 || secondSyn.count == 5))
                                ||(secondSyn.name.equals(synergymain.getDemolitionist().name) &&(secondSyn.count == 2 || secondSyn.count == 3))
                                ||(secondSyn.name.equals(synergymain.getManaReaver().name) &&(secondSyn.count == 2 || secondSyn.count == 3))) secondSyn.synPower = 3

                        if (secondSyn.name.equals(synergymain.getStarship().name)|| secondSyn.name.equals(synergymain.getParagon().name)){
                            secondSyn.synPower = 3
                        }

                        addChamp(simChamp)
                        addSynergy(firstSyn)
                        addSynergy(secondSyn)



                    }

                    if (champ?.synergy?.size == 3) {
                        val simChamp = SimulatorChamp(
                            champ?.name,
                            champ?.imgPath,
                            champ?.cost,
                            champ?.synergy[0]?.name,
                            champ?.synergy[1]?.name,
                            champ?.synergy[2]?.name
                        )
                        val firstSyn =
                            SimulatorSynergy(
                                champ?.synergy[0]?.name,
                                champ?.synergy[0]?.imgPath,
                                1,
                                0
                            )
                        val secondSyn =
                            SimulatorSynergy(
                                champ?.synergy[1]?.name,
                                champ?.synergy[1]?.imgPath,
                                1
                            ,0
                            )
                        val thirdSyn =
                            SimulatorSynergy(
                                champ?.synergy[2]?.name,
                                champ?.synergy[2]?.imgPath,

                                1
                            ,0

                            )

                        simulatorViewModel.getAllSynergy()
                            .observe(viewLifecycleOwner, Observer { synergy ->
                                synergy?.let { synergy ->
                                    for (syn in synergy) {

                                        if (firstSyn.name.equals(syn.name)) {
                                           // var power = getSynPower(syn)
                                            firstSyn.count = syn.count?.plus(1)
                                          //  firstSyn.synPower = power
                                            Log.e("power",  firstSyn.synPower.toString())
                                        }

                                        if (secondSyn.name.equals(syn.name)) {
                                          //  var power = getSynPower(syn)
                                            secondSyn.count = syn.count?.plus(1)
                                          //  secondSyn.synPower = power
                                            Log.e("power",  secondSyn.synPower.toString())
                                        }

                                        if (thirdSyn.name.equals(syn.name)) {
                                        //    var power = getSynPower(syn)
                                            thirdSyn.count = syn.count?.plus(1)
                                          //  thirdSyn.synPower = power
                                        }

                                    }
                                }
                            })

                        if ((firstSyn.name.equals(synergymain.getSpacePirate().name) && (firstSyn.count == 2 || firstSyn.count == 3))
                                ||(firstSyn.name.equals(synergymain.getCybernetic().name) && (firstSyn.count == 3 || firstSyn.count == 4 || firstSyn.count == 5))) firstSyn.synPower = 1


                        if ((firstSyn.name.equals(synergymain.getCybernetic().name) && (firstSyn.count == 6 || firstSyn.count == 7))
                                ||(firstSyn.name.equals(synergymain.getSpacePirate().name) && (firstSyn.count == 4))) firstSyn.synPower = 3

                        if((secondSyn.name.equals(synergymain.getBlademaster().name) &&(secondSyn.count == 3 || secondSyn.count == 4 || secondSyn.count == 5)))
                            secondSyn.synPower = 1

                        if((secondSyn.name.equals(synergymain.getBlademaster().name) &&(secondSyn.count == 6 || secondSyn.count == 7))
                                ||(secondSyn.name.equals(synergymain.getDemolitionist().name) &&(secondSyn.count == 2 || secondSyn.count == 3))) secondSyn.synPower = 3

                        if((thirdSyn.name.equals(synergymain.getManaReaver().name) &&(secondSyn.count == 2 || secondSyn.count == 3))) thirdSyn.synPower = 3

                        if (thirdSyn.name.equals(synergymain.getMercenary().name)){
                            thirdSyn.synPower = 3
                        }
                        addChamp(simChamp)
                        addSynergy(firstSyn)
                        addSynergy(secondSyn)
                        addSynergy(thirdSyn)

                    }

                }

            }

        })

        recyclerView?.layoutManager = LinearLayoutManager(view!!.context)
        recyclerView?.adapter = mAdapter
    }

   /* private fun getSynPower(synergy: SimulatorSynergy) : Int{
        val synergymain: SynergyData = SynergyData()
        var power : Int = 0
        if (((synergy.count == 1 || synergy.count == 2) && synergy.imgPath == synergymain.getBattlecast().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getChrono().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getSorcerer().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getBrawler().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getMystic().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getVanguard().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getInfiltrator().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getDarkStar().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getCelestial().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getSpacePirate().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getProtector().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getSniper().imgPath)
            || ((synergy.count == 1 || synergy.count == 2)  && synergy.imgPath == synergymain.getBlaster().imgPath))
                power = 1


        if ( ((synergy.count == 2 || synergy.count == 3 || synergy.count == 4) && synergy.imgPath == synergymain.getCybernetic().imgPath)
            || ((synergy.count == 2 || synergy.count == 3 || synergy.count == 4) && synergy.imgPath == synergymain.getBlademaster().imgPath)
            || ((synergy.count == 2 || synergy.count == 3 || synergy.count == 4) && synergy.imgPath == synergymain.getRebel().imgPath)
            || ((synergy.count == 2 || synergy.count == 3 || synergy.count == 4) && synergy.imgPath == synergymain.getStarGuardian().imgPath))
                power = 1

        if (((synergy.count == 3 || synergy.count == 4) && synergy.imgPath == synergymain.getBattlecast().imgPath)
            ||((synergy.count == 3 || synergy.count == 4)&& synergy.imgPath == synergymain.getSorcerer().imgPath)
            ||((synergy.count == 3 || synergy.count == 4)&& synergy.imgPath == synergymain.getVanguard().imgPath)
            ||((synergy.count == 3 || synergy.count == 4)&& synergy.imgPath == synergymain.getDarkStar().imgPath)
            ||((synergy.count == 3 || synergy.count == 4)&& synergy.imgPath == synergymain.getCelestial().imgPath)
            ||((synergy.count == 3 || synergy.count == 4)&& synergy.imgPath == synergymain.getChrono().imgPath))
                power = 2
        if ((synergy.count == 1 && synergy.imgPath == synergymain.getManaReaver().imgPath)
            || (synergy.count == 1 && synergy.imgPath == synergymain.getDemolitionist().imgPath))
                power = 3

        if ((synergy.count == 2 && synergy.imgPath == synergymain.getMechPilot().imgPath)
            || (synergy.count == 2 && synergy.imgPath == synergymain.getAstro().imgPath))
                power = 3


        if (((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getBrawler().imgPath)
            || ((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getMystic().imgPath)
            || ((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getSpacePirate().imgPath)
            || ((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getSniper().imgPath)
            || ((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getBlaster().imgPath)
            || ((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getInfiltrator().imgPath)
            || ((synergy.count == 3 || synergy.count == 4 || synergy.count == 5) && synergy.imgPath == synergymain.getProtector().imgPath))
                power = 3

        if (synergy.count == 5&& synergy.imgPath == synergymain.getStarGuardian().imgPath
            || (synergy.count == 5 && synergy.imgPath == synergymain.getChrono().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getCybernetic().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getDarkStar().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getRebel().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getBattlecast().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getCelestial().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getBlademaster().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getSorcerer().imgPath)
            || (synergy.count == 5 && synergy.imgPath == synergymain.getVanguard().imgPath))
                power = 3


        Log.e("synPower",synergy.name + "  " + power.toString())

        return  power
    }*/

    private fun initSearchBar() {
        var searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                (recyclerView?.adapter as ChampMainAdapter).filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (recyclerView?.adapter as ChampMainAdapter).filter(newText)
                return false
            }
        })
    }


}


