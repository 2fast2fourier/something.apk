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

    $('.quote').on('click', function(event) {
         listener.onQuoteClick($(this).parent().parent().attr('id').replace(/post/,''));
    });
    $('.edit').on('click', function(event) {
        listener.onEditClick($(this).parent().parent().attr('id').replace(/post/,''));
    });
    $('.more').on('click', function(event) {
        listener.onMoreClick($(this).parent().parent().attr('id').replace(/post/,''), $(this).attr('username'), $(this).attr('userid'));
    });
    $('.lastread').on('click', function(event) {
        listener.onLastReadClick($(this).attr('lastreadid'));
    });

    $('.bbc-spoiler').removeAttr('onmouseover');
    $('.bbc-spoiler').removeAttr('onmouseout');
    $('.bbc-spoiler').click( function(){ $(this).toggleClass('spoiled');});

    // hide-old posts
    if($('.toggleread').length > 0){
        $('.read').hide();
    }else{
        $('.seenimg').each(function(index, item){
            item.src = item.getAttribute('hideimg');
        });
    }
    $('.toggleread').click(function(event) {
        showReadPosts();
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
	        if($(id).size() > 0){
	            $(this).click(function(e){
	                e.preventDefault();
	                if($(id).css("display") == 'none'){
                        showReadPosts();
                        window.jumpToPostId = id;
                        window.setTimeout(jumpToJump, 500);
	                }else{
	                    window.scrollTo(0,$(id).offset().top);
	                }
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

function jumpToJump(){
    if(window.jumpToPostId > 0){
        window.scrollTo(0,$(jumpTo).offset().top);
        window.jumpToPostId = 0;
    }
}

function showReadPosts(){
    $('.read').show();
    $('.seenimg').each(function(index, item){
        item.src = item.getAttribute('hideimg');
    });
    $('.toggleread').hide();
}

function onThreadLoaded(){
    pageinit();
    if(window.jumpToPostId > 0){
        scrollPost(window.jumpToPostId);
        window.jumpToPostId = 0;
    }else{
        scrollLastRead();
    }
}

function scrollPost(postId) {
    try{
        window.topScrollItem = $("#post"+postId).first();
        window.topScrollPos = window.topScrollItem.offset().top;
        window.scrollTo(0,window.topScrollPos);
        window.topScrollCount = 100;
        window.topScrollID = window.setTimeout(scrollUpdate, 500);
    }catch(error){
        window.topScrollCount = 0;
        window.topScrollItem = null;
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