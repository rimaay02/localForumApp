import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ForumService } from '../services/forum.service';
import { AuthService } from '../services/auth.service';
import { Subscription } from 'rxjs';
import { MessageService } from '../services/message.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';


@Component({
  selector: 'app-room',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.css']
})
export class RoomComponent implements OnInit, OnDestroy {
  roomId: string | null = null;
  roomData: any = { id: 0, title: "", message: "", creatorName: "", answers: [] };
  newAnswer: string = '';
  userId: number = 0;
  currentUser: any = null; 
  private roomDataSubscription: Subscription | null = null;
  private userSubscription: Subscription | null = null;
  safeRoomMessage: SafeHtml = ''; 
  safeAnswer: SafeHtml = "";

  constructor(
    private route: ActivatedRoute,
    private forumService: ForumService,
    private authService: AuthService,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.userSubscription = this.authService.currentUsername$.subscribe(username => {
      this.currentUser = username ? { username: username } : null;
    });
    this.userId = this.authService.getCurrentUserId() || 0;
    this.route.paramMap.subscribe(params => {
      this.roomId = params.get('id');
      if (this.roomId) {
        this.loadRoomData(this.roomId);
      }
    });

    this.roomDataSubscription = this.forumService.roomData$.subscribe(roomData => {
      if (roomData) {
        this.roomData = roomData;
        this.safeRoomMessage = this.sanitize(roomData.message); // Sanitize room message
        this.sanitizeAnswer(); 
        this.cdr.detectChanges(); 
      }
    });
  }

  sanitize(content: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(content);
  }
  sanitizeAnswer(): void {
    this.safeAnswer = this.sanitize(this.roomData.message);
  }


  ngOnDestroy(): void {
    if (this.roomDataSubscription) {
      this.roomDataSubscription.unsubscribe();
    }
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
    }
  }

  async loadRoomData(roomId: string): Promise<void> {
    try {
      this.roomData = await this.forumService.getRoomById(+roomId);
      this.safeRoomMessage = this.sanitize(this.roomData.message); 
      await this.loadAnswers(+roomId); 
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Failed to load room data', error);
      this.messageService.showMessage('Failed to load room data.', false);
    }
  }

  async loadAnswers(roomId: number): Promise<void> {
    try {
      const answers = await this.forumService.getAnswersByRoomId(roomId.toString());
      for (let i = 0; i < answers.length; i++) {
        const answer = answers[i];
        console.log(answer)
        let votes = await this.forumService.getVotesByAnswerId(answer.id);
        
        for (let i in votes) {
          answer.votesLength = votes[i].length;
          this.roomData.answers = answers;
          this.sanitizeAnswer();
          this.cdr.detectChanges();
        }
      }
    } catch (error) {
      console.error('Failed to load answers', error);
      this.messageService.showMessage('Failed to load answers.', false);
    }
  }
  

  async sendAnswer(): Promise<void> {
    if (this.newAnswer.trim() === '') {
      this.messageService.showMessage('Answer cannot be empty.', false);
      return;
    }

    if (this.roomId && this.newAnswer && this.currentUser) {
      const answer = {
        roomId: this.roomId,
        message: this.newAnswer,
        userId: this.userId
      };
      try {
        await this.forumService.postAnswer(answer);
        this.newAnswer = '';
        this.messageService.showMessage('Answer posted successfully.', true);
        await this.loadRoomData(this.roomId);
        this.cdr.detectChanges(); 
      } catch (error) {
        console.error('Failed to post answer', error);
        this.messageService.showMessage('Failed to post answer.', false);
      }
    }
  }

  async voteUp(answer: any): Promise<void> {
    try {
      await this.forumService.insertVote(this.userId, answer.id);
      await this.loadAnswers(this.roomData.id);
    } catch (error) {
      console.error('Failed to upvote', error);
      this.messageService.showMessage('You have used your upvote', false);
    }
  }

  async voteDown(answer: any): Promise<void> {
    try {
      await this.forumService.deleteVote(this.userId, answer.id);
      await this.loadAnswers(this.roomData.id);
      this.messageService.showMessage('You now have no vote in this answer', true);
    } catch (error) {
      console.error('Failed to downvote', error);
      this.messageService.showMessage('Failed to downvote.', false);
    }
  }

  async deleteAnswer(answerId: number): Promise<void> {
    const confirmation = confirm('Are you sure you want to delete this answer?');
    if (confirmation) {
      try {
        await this.forumService.deleteAnswer(answerId);
        this.messageService.showMessage('Answer deleted successfully.', true);
        if (this.roomId) {
          await this.loadRoomData(this.roomId);
        }
        this.cdr.detectChanges();
      } catch (error) {
        console.error('Failed to delete answer', error);
        this.messageService.showMessage('Failed to delete answer.', false);
      }
    }
  }
}