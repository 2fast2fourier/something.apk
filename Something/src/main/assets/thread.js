function toggleinfo(info){
    if($(info).children('.postinfo-title').hasClass('extended')){
        $(info).children('.avatar-cell').removeClass('extended');
        $(info).children('.avatar-cell').children('.avatar').removeClass('extended');
        $(info).children('.postinfo-title').removeClass('extended');
        $(info).children('.postinfo-regdate').removeClass('extended');
    }else{
        $(info).children('.avatar-cell').addClass('extended');
        $(info).children('.avatar-cell').children('.avatar').addClass('extended');
        $(info).children('.postinfo-title').addClass('extended');
        $(info).children('.postinfo-regdate').addClass('extended');
    }
}

function toggleoptions(menu){
    $(menu).parent().parent().children('.postoptions').toggleClass('extended');
}


function pageinit() {
    $('.bbc-spoiler').removeAttr('onmouseover');
    $('.bbc-spoiler').removeAttr('onmouseout');
    $('.bbc-spoiler').click( function(){ $(this).toggleClass('spoiled');});

    // hide-old posts
    if($('.toggleread').length > 0){
        $('.read').hide();
    }
    $('.toggleread').click(function(event) {
        $('.read').show();
          $('.toggleread').hide();
          window.setTimeout(scrollLastRead, 200);
    });
    
    $('.postinfo').on('click',function(){
        toggleinfo($(this));
    });
    $('.postmenu').on('click',function(){
        toggleoptions($(this));
    });

    $('.quote_link').each(function(){
        var id = this.hash;
        try{
	        if($(id).size() > 0 && $(id).css("visibility") !== "none"){
	            $(this).click(function(e){
	                window.scrollTo(0,$(id).offset().top);
	                e.preventDefault();
	            });
	        }
        }catch(error){
        	console.log(error);
        }
    });

    $('.timg').on('click',function () {
        $(this).removeClass('timg');
        if(!$(this).parent().is('a')){
            $(this).wrap('<a href="'+$(this).attr('src')+'" />');
        }
    });
};

function onThreadLoaded(){
    pageinit();
    scrollLastRead();
}

function scrollPost(postjump) {
    if (postjump != "") {
        try{
            window.topScrollItem = $("#post"+postjump).first();
            window.topScrollPos = window.topScrollItem.offset().top;
            window.scrollTo(0,window.topScrollPos);
            window.topScrollCount = 200;
              window.topScrollID = window.setTimeout(scrollUpdate, 500);
        }catch(error){
            scrollLastRead();
        }
    } else {
        scrollLastRead();
    }
}

function scrollLastRead(){
    try{
        window.topScrollItem = $('.unread').first();
        window.topScrollPos = window.topScrollItem.offset().top;
        window.topScrollCount = 100;
        window.scrollTo(0, window.topScrollPos);
          window.topScrollID = window.setTimeout(scrollUpdate, 500);
    }catch(error){
        window.topScrollCount = 0;
        window.topScrollItem = null;
    }
}

function scrollUpdate(){
    try{
        if(window.topScrollCount > 0 && window.topScrollItem){
            var newpos = window.topScrollItem.offset().top;
            if(newpos-window.topScrollPos > 0){
                window.scrollBy(0, newpos-window.topScrollPos);
            }
            window.topScrollPos = newpos;
            window.topScrollCount--;
            window.topScrollID = window.setTimeout(scrollUpdate, 200);
        }
    }catch(error){
        window.topScrollCount = 0;
        window.topScrollItem = null;
    }
}