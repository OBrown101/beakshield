package com.beakshield

import com.beakshield.dawson.Dawson
import com.beakshield.viewModels.AgentsScreenViewModel
import com.beakshield.viewModels.BaseScreenViewModel
import com.beakshield.viewModels.ChatsScreenViewModel
import com.beakshield.viewModels.DecreesScreenViewModel
import com.beakshield.viewModels.KnowledgeScreenViewModel
import com.beakshield.viewModels.MainScreenViewModel
import com.beakshield.viewModels.ProfileScreenViewModel
import com.beakshield.viewModels.SkillsScreenViewModel
import com.beakshield.viewModels.SystemScreenViewModel

object BeakShieldApp {

    val preferences: Preferences by lazy {
        Preferences()
    }

    val dawson by lazy {
        Dawson()
    }

    val baseScreenViewModel by lazy {
        BaseScreenViewModel()
    }

    val mainScreenViewModel by lazy {
        MainScreenViewModel()
    }

    val chatsScreenViewModel by lazy {
        ChatsScreenViewModel()
    }

    val agentsScreenViewModel by lazy {
        AgentsScreenViewModel()
    }

    val knowledgeScreenViewModel by lazy {
        KnowledgeScreenViewModel()
    }

    val decreesScreenViewModel by lazy {
        DecreesScreenViewModel()
    }

    val skillsScreenViewModel by lazy {
        SkillsScreenViewModel()
    }

    val profileScreenViewModel by lazy {
        ProfileScreenViewModel()
    }

    val systemScreenViewModel by lazy {
        SystemScreenViewModel()
    }

    fun onCreate() {
        preferences
        dawson

        baseScreenViewModel
        mainScreenViewModel
        chatsScreenViewModel
        agentsScreenViewModel
        knowledgeScreenViewModel
        decreesScreenViewModel
        skillsScreenViewModel
        profileScreenViewModel
        systemScreenViewModel
    }
}